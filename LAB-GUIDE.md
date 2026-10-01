# Spring Boot Performance and Security Lab

## Project Structure

```text
src/main/java/com/example/demo/
  controller/  HTTP routes for posts and authentication; controllers translate requests and delegate.
  service/     Use-case logic; services coordinate repository access and caching.
  repository/  Spring Data JPA interfaces; query methods keep database work out of controllers.
  model/       Database entities; Post has lazily loaded comments for the fetch-join lesson.
  dto/         Request/response shapes; DTOs keep database entities out of the public API.
  security/    JWT issue/validation, the request filter, and endpoint/method access rules.
  config/      Application-wide Spring configuration, including enabling the cache abstraction.
  util/        Small cross-cutting helpers, including authenticated AES-GCM encryption.
src/main/resources/
  application.properties  Local database, cache, and environment-driven demo settings.
  ehcache.xml             Bounded Ehcache store and expiration policy.
```

Each annotation has a job: `@RestController` exposes HTTP, `@Service` registers use-case logic as a Spring bean, and `@Repository` marks the persistence boundary. Constructor injection is Dependency Injection: Spring creates each managed object and passes in its dependencies, instead of each class constructing its own collaborators.

## Request and Data Flow

```text
HTTP request -> SecurityFilterChain -> JwtAuthenticationFilter -> Controller
             -> Service -> Repository (JPQL/SQL) -> Database
```

On a protected request, the JWT filter checks the bearer token before the controller runs. The controller validates and routes; the service applies use-case logic; the repository asks the database for rows. A `Page<T>` includes content plus count/page metadata, so clients can request only the slice they need.

## 1. Pageable Reads, Sorting, and Filtering

### Step 1: Naive version

```java
// Problem: findAll() loads every row, uses memory proportional to table size,
// and gives a caller no stable way to request a particular slice or ordering.
List<Post> posts = postRepository.findAll();
```

### Step 2: Why it hurts

A table with a million posts should not mean a million Java objects for a page-one request. Filtering after `findAll()` has the same problem: the database has already sent all rows over the connection.

### Step 3: Improved version in this project

`GET /api/posts` accepts Spring Data `Pageable`, plus optional `title` and `author` filters. The service builds a JPA `Specification`, so supplied filters become database predicates, then calls `findAll(filters, pageable)`. `spring.data.web.pageable.max-page-size=100` prevents an enormous caller-selected page.

```http
GET http://localhost:8080/api/posts?page=0&size=10&sort=createdAt,desc
GET http://localhost:8080/api/posts?page=1&size=10&sort=title,asc&author=ada
GET http://localhost:8080/api/posts?title=spring&author=lee
```

Pages are zero-based: `page=0` is the first page. Sorting uses entity property names (`createdAt`, `title`), not database column names.

### Step 4: Why it works

Spring translates the `Pageable` and `Specification` into SQL limit/offset, ordering, and `WHERE` predicates. Only the selected rows are mapped into response DTOs. The response's `data` is a Spring `Page`, including `content`, `number`, `size`, `totalElements`, and `totalPages`.

## 2. JPQL, JOIN FETCH, and the N+1 Problem

JPQL (Java Persistence Query Language) is a query language for Java entities. It uses entity and property names, not table/column names. `JOIN FETCH` loads a relationship in the same query, instead of lazily loading it later. The N+1 problem occurs when a list query loads N rows, then each row's lazy relationship triggers another query.

### Step 1: Naive version

```java
// Problem: one query loads posts, then accessing comments can issue one query per post.
List<Post> posts = postRepository.findAll();
posts.forEach(post -> post.getComments().size());
```

### Step 2: Why it hurts

For 50 posts, code can issue 1 query for posts plus 50 more for comments. That is the N+1 problem. `Post.comments` is lazy so list endpoints do not pay for data they do not need.

### Step 3: Improved JPQL in this project

```java
@Query("select distinct p from Post p left join fetch p.comments where p.id = :id")
Optional<Post> findByIdWithComments(@Param("id") Long id);
```

The single-post service calls this method and includes comment bodies in its response. JPQL uses Java entity/property names (`Post`, `comments`); Hibernate translates it to SQL for the configured database.

### Step 4: Why it works, and its boundary

The join loads the post and its comments in one round trip. `distinct` prevents duplicate root posts when multiple comments match. Do not apply a collection fetch join casually to a paged list: SQL row multiplication can corrupt page boundaries and totals. For a list page, prefer a DTO projection, batch fetching, or a two-query strategy.

Add a comment using an authenticated user, then request that post to see the eagerly fetched comment text:

```http
POST http://localhost:8080/api/posts/1/comments
Authorization: Bearer <accessToken>
Content-Type: application/json

{"body":"The page query keeps result sets small."}
```

## 3. Native SQL Example

`PostRepository.findNewestPostsNative(Pageable)` demonstrates an explicitly written SQL query and its `countQuery`. Native SQL can help when database-specific features or measured query tuning justify it, but it couples the query to table/column naming. JPQL is the portable default. Always profile with realistic data before optimizing.

## 4. Ehcache

### Step 1: Naive version

```java
// Problem: repeating this lookup performs the same database trip on every request.
return postRepository.findByIdWithComments(id);
```

### Step 2: What caching changes

The first request reads the database and saves its response under the post ID. A later request with that key can return the saved value without running the method. Ehcache holds up to 1,000 entries for up to five minutes; this is a bounded in-process cache, not shared state between application instances.

### Step 3: Improved version in this project

```java
@Cacheable(value = "postsById", key = "#id")
public PostResponseDto getPostById(Long id) { ... }
```

`@EnableCaching` switches on Spring's cache proxy. `ehcache.xml` declares the cache, and `@CacheEvict` clears entries after create/delete. Spring checks the cache before entering the method; on a miss it executes the method and stores the returned DTO.

### Step 4: Why invalidation matters

A cache is a saved answer. Without eviction or expiry it may keep telling the client an old answer after data changes. Cache only data whose freshness rules you understand, choose keys that include all inputs that affect the answer, and avoid caching user-specific/private data in a shared key. Multi-instance production deployments usually need a shared cache such as Redis or a deliberate per-instance consistency policy.

## 5. JWT Authentication and Refresh

### Step 1: Naive version

```java
// Problem: sending a username and password on every API call exposes reusable credentials
// repeatedly and requires server-side session state if the server wants to remember the login.
```

### Step 2: Token model

After login, the server returns a short-lived access token (15 minutes) and a refresh token (7 days). A JWT is like a signed ID card: its signature shows it was issued by the server, but its payload is readable, not encrypted. Do not put passwords or secrets in claims.

### Step 3: Try it

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username":"demo-user","password":"change-me-user"}
```

Use the returned `accessToken` on a protected request:

```http
POST http://localhost:8080/api/posts
Authorization: Bearer <accessToken>
Content-Type: application/json

{"title":"Paging matters","content":"Fetch only the needed rows.","author":"Ada"}
```

Renew tokens before the access token expires:

```http
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{"refreshToken":"<refreshToken>"}
```

The refresh endpoint returns a new access/refresh pair. Because this teaching implementation is stateless, an old refresh token remains usable until expiry; production-grade one-time rotation requires server-side token-family/revocation tracking. Keep refresh tokens in an appropriately protected client store (for browser apps, prefer secure HttpOnly cookies with CSRF design); never log either token.

#### How AES-GCM, JWT Access and Refresh Tokens Work Together?

Answer: `AesGcmEncryption` encrypts third-party credentials for storage, while `JwtService` issues signed JWTs for authentication. The access token is short-lived and used for API requests, while the refresh token is longer-lived and used to obtain new access tokens. Both tokens are validated by the server to ensure authenticity and integrity.

### Step 4: What validates the JWT

`JwtService` signs tokens with an HMAC key of at least 32 decoded bytes, then checks signature, expiration, subject, and a `type` claim. The `type` prevents a refresh token from being accepted as an access token. `JwtAuthenticationFilter`, a `OncePerRequestFilter`, reads `Authorization: Bearer ...`, loads that user's current authorities, and populates Spring's security context.

## 6. SecurityFilterChain and RBAC

`SecurityConfiguration` builds the `SecurityFilterChain`: the ordered security checkpoint line for web requests. It disables form/basic login, uses stateless sessions, permits login/refresh and public GET posts, requires authentication for other routes, and places the custom JWT filter before Spring's username/password filter. Demo users are `demo-user` (`USER`) and `demo-admin` (`USER`, `ADMIN`). Passwords are BCrypt encoded.

The delete controller also uses `@PreAuthorize("hasRole('ADMIN')")`. Endpoint rules answer “must this request be logged in?”; method security answers “does this principal have the specific authority for this operation?” A normal user receives 403 for deletion; an admin can proceed.

```http
DELETE http://localhost:8080/api/posts/1
Authorization: Bearer <admin-accessToken>
```

## 7. AES-GCM for Third-Party Credentials

`AesGcmEncryption` encrypts a credential using AES-256-GCM. GCM authenticates as well as encrypts, so modified ciphertext fails decryption. A fresh 12-byte random nonce is stored alongside each ciphertext; the nonce is not secret and must never be reused with the same key. The AES key belongs in a secret manager/KMS, not source control, a database row, or an image. The property default exists only to make the local lab runnable and is not secure.

```java
String encrypted = aesGcmEncryption.encrypt("third-party-api-key");
String original = aesGcmEncryption.decrypt(encrypted);
```

Never print either value in production logs. Plan key versioning and rotation before storing credentials long-term.

## 8. Common Mistakes

- **N+1:** Loading a list and lazily reading each row's relationship runs many round trips. Fetch the needed relationship for a bounded detail query, or use a projection/batch approach for lists.
- **JWT says invalid:** Check that the issuer and validator use the same base64-decoded key bytes, the token has not expired, and the request sends the raw token after `Bearer `. A refresh token is not an access token. Never fix this by disabling signature/expiry checks.
- **Cache looks stale:** Add update-path eviction as well as create/delete eviction; use a short expiry; include every input in cache keys; understand that Ehcache is local to one process.
- **Secret is “encrypted” but exposed:** Base64 is encoding, not encryption. Keep AES/JWT keys out of source control and logs, and use unique GCM nonces.
- **Fetch join plus pagination:** A collection join duplicates SQL rows. Do not use that shape for pageable root results without proving the generated SQL and count behavior.
- **Production auth limitations:** The demo uses in-memory users, local fallback keys, no account lockout, and stateless refresh tokens without revocation. Replace these before deployment; use TLS and a managed secret store.

## 9. Run and Test

Run the suite:

```powershell
.\mvnw.cmd test
```

Start locally with:

```powershell
.\mvnw.cmd spring-boot:run
```

For production-like local configuration, set `JWT_SECRET_BASE64`, `AES_KEY_BASE64`, `DEMO_USER_PASSWORD`, and `DEMO_ADMIN_PASSWORD` in the process environment. Both key variables must decode to 32 bytes. The fallback values in `application.properties` are public lab examples.

To exercise filtering, create several posts through the admin token, then call:

```http
GET http://localhost:8080/api/posts?page=0&size=2&sort=createdAt,desc&author=Ada
GET http://localhost:8080/api/posts?page=0&size=2&sort=title,asc&title=paging
GET http://localhost:8080/api/posts/1
```

H2 console is enabled for local learning. Do not expose it in a production deployment.