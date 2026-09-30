````markdown
# Performance and Security Lab

Start with the [structure-first lab guide](LAB-GUIDE.md) for the project map, staged explanations, request examples, common mistakes, and run instructions.

```text
controller/  HTTP endpoints for posts and authentication
service/     Use-case logic and cache coordination
repository/  Spring Data JPA queries and database access
model/       Post and comment database entities
dto/         Validated request and response shapes
security/    JWT service, request filter, and access rules
config/      Spring cache configuration
util/        AES-GCM credential encryption helper
```

# Spring Boot Fundamentals

This file explains the core ideas behind a Spring Boot application, how it works internally, how Java code is compiled, how dependency injection works, how Spring manages objects, and how a web request flows through a servlet-based server.

This README is designed for absolute beginners who already know the basics of backend development in Node.js, so the ideas are explained in a way that connects to familiar concepts such as Express, dependency injection, object creation, routing, and server startup.

---

## 1. What is Spring Boot?

Spring Boot is a framework built on top of the Spring Framework. It helps developers create production-ready Java applications quickly with minimal configuration.

It is designed to:
- reduce boilerplate configuration
- provide embedded web servers
- enable auto-configuration
- support REST APIs, web apps, scheduled jobs, data access, messaging, etc.
- integrate well with Maven and Gradle

In simple terms:
- Spring gives the application framework and inversion of control
- Spring Boot makes it easier to start and run the application with sensible defaults

Think of it like this in Node.js terms:
- Express is a web framework for Node.js
- Spring Boot is a full Java framework ecosystem that gives you a web server, dependency injection, configuration system, bean management, and many built-in features out of the box

If you are comfortable with Node.js, then Spring Boot is the Java equivalent of:
- Express + app bootstrap + routing + config + service architecture + dependency injection + application lifecycle management

---

## 2. Why Spring Boot is popular

Spring Boot is widely used because it makes Java development faster and simpler.

Benefits:
- Minimal setup
- Embedded Tomcat/Jetty server
- Starter dependencies
- Production-ready features
- Easy configuration via `application.properties` or `application.yml`
- Works well with REST APIs and microservices

---

## 3. Basic Terminologies

### 3.1 Java
Java is a general-purpose, object-oriented programming language. A Spring Boot project is a Java project.

Key ideas:
- classes
- objects
- methods
- inheritance
- encapsulation
- polymorphism

---

### 3.2 Maven
Maven is a build and dependency management tool used in Java projects.

It manages:
- dependencies
- build lifecycle
- packaging
- project structure
- plugin execution

The file `pom.xml` is the central Maven configuration.

Example:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

This tells Maven:
- download the Spring Boot web starter
- include required libraries automatically

Node.js comparison:
- In Node.js, you usually use `package.json` and `npm install`
- In Java/Spring Boot, you use `pom.xml` and Maven to download jars and manage the project

Example Node.js equivalent:
```json
{
  "dependencies": {
    "express": "^4.18.0"
  }
}
```

---

### 3.3 Spring Framework
Spring is a large Java ecosystem for building applications using enterprise patterns.

It provides:
- dependency injection
- AOP
- web MVC
- transactions
- security
- data access

Spring Boot is a layer on top of Spring that reduces configuration.

---

### 3.4 Spring Container
The Spring container is the runtime environment that creates, configures, and manages application objects. These objects are called beans.

The container is responsible for:
- creating beans
- wiring dependencies
- managing bean lifecycle
- destroying beans when no longer needed

Two main container types:
- `BeanFactory` — basic container
- `ApplicationContext` — advanced container used by Spring Boot

---

### 3.5 Bean
A bean is any object managed by Spring.

Examples:
- service class
- repository class
- controller class
- configuration class

Spring creates and manages these objects.

Think of a bean like a Node.js module instance that is created and shared by a framework for you.

Example in Node.js terms:
- In Express, you may create a module like `userService.js` and import it wherever needed
- In Spring, a class marked with `@Service` becomes a bean and Spring can inject it anywhere automatically

```java
@Service
public class UserService {
    public String getMessage() {
        return "Hello";
    }
}
```

Now Spring can manage this `UserService` object for you.

---

### 3.6 Dependency
A dependency is an object required by another object.

Example:
```java
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Here, `UserRepository` is a dependency of `UserService`.

---

### 3.7 Dependency Injection (DI)
Dependency Injection means giving an object its dependencies from outside rather than creating them internally.

Benefits:
- loose coupling
- easier testing
- cleaner code
- easier maintenance

Types of DI:
- Constructor injection
- Setter injection
- Field injection

Example:
```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Spring automatically provides `UserRepository` to `UserService` if it is registered as a bean.

Node.js analogy:
- In Node.js, you often do something like:
```js
const userRepository = require('./userRepository');
const userService = new UserService(userRepository);
```
- In Spring, instead of manually creating the dependency, the framework injects it for you.

This is the same idea as passing dependencies into a class constructor, but the object creation is handled by the Spring container.

---

### 3.8 Inversion of Control (IoC)
Inversion of Control means the control of object creation and lifecycle is given to the framework instead of the developer writing manual `new` logic.

Instead of:
```java
UserService service = new UserService(new UserRepository());
```

Spring does this for you.

This is the core idea behind Spring.

Node.js analogy:
- In a Node.js app, you manually `require()` the modules and create instances yourself
- In Spring, the framework creates the objects and decides when and how they should be used

Example:
```java
@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

You do not manually call `new UserService(new UserRepository())` everywhere. Spring does it when the application starts.

---

### 3.9 IoC vs DI
These are closely related but not the same.

- IoC: framework controls object creation and lifecycle
- DI: dependencies are injected into objects

Think of it like this:
- IoC is the principle
- DI is the implementation pattern

Easy comparison:

| Concept | Node.js idea | Spring idea |
|---|---|---|
| Manual object creation | `new UserService()` | Spring creates it automatically |
| Dependency passing | `const repo = require(...)` | Spring injects the dependency |
| Framework control | You decide when to instantiate | Spring controls lifecycle |

---

### 3.10 Component Scanning
Spring scans packages to find classes marked with annotations such as:
- `@Component`
- `@Service`
- `@Repository`
- `@Controller`
- `@RestController`
- `@Configuration`

These classes are registered as beans.

Node.js analogy:
- In Node.js, if you import a module manually, you know exactly which file you are loading
- In Spring, the framework scans a package and automatically discovers components without you writing all wiring code manually

This is one of the reasons Spring apps feel larger and more framework-driven than Express apps.

---

### 3.11 Annotation Types

#### `@SpringBootApplication`
This is the main annotation for a Spring Boot application class.

Example:
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

It combines:
- `@Configuration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

---

#### `@Component`
Marks a class as a Spring-managed bean.

```java
@Component
public class MyService {
}
```

---

#### `@Service`
Used for business logic classes.

```java
@Service
public class UserService {
}
```

---

#### `@Repository`
Used for database access classes.

```java
@Repository
public class UserRepository {
}
```

---

#### `@Controller`
Used for MVC web pages or endpoints if building traditional MVC.

```java
@Controller
public class HomeController {
}
```

---

#### `@RestController`
Used for REST API classes. It combines `@Controller` and `@ResponseBody`.

```java
@RestController
public class UserController {
    @GetMapping("/users")
    public String getUsers() {
        return "Hello";
    }
}
```

---

#### `@Autowired`
Injects dependency automatically.

```java
@Autowired
private UserService userService;
```

Modern best practice prefers constructor injection.

Node.js comparison:
- In Node.js, dependency injection often looks like passing a module/object into a function or constructor
- In Spring, `@Autowired` is the framework doing this for you automatically

---

#### `@Configuration`
Marks a class as a configuration class for bean declarations.

```java
@Configuration
public class AppConfig {
    @Bean
    public MyBean myBean() {
        return new MyBean();
    }
}
```

---

#### `@Bean`
Used to define a bean explicitly.

---

#### `@Value`
Injects values from configuration files.

```java
@Value("${app.name}")
private String appName;
```

---

#### `@RequestMapping`
Maps HTTP requests to controller methods.

```java
@RequestMapping("/hello")
public String hello() {
    return "Hello";
}
```

---

#### `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
Specific REST mappings for HTTP methods.

---

### 3.12 Application Context
The `ApplicationContext` is the central Spring runtime component. It holds all beans and is responsible for all object lifecycle management.

When Spring Boot starts, it creates an application context and registers the beans.

---

## 4. How a Spring Boot Application Works

The startup flow is:

1. Java program starts at `main()`
2. `SpringApplication.run(...)` is called
3. Spring Boot creates the application context
4. It scans for components and beans
5. Auto-configuration loads default settings
6. Embedded web server starts
7. HTTP requests are handled by servlet and controller layers

Example:
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

This is the entry point of a Spring Boot application.

Node.js analogy:
- In Node.js, a server starts when you call something like `app.listen(3000)`
- In Spring Boot, the app starts when `SpringApplication.run(...)` is invoked
- The framework boots up the Spring context, registers beans, configures libraries, and starts Tomcat automatically

A simplified comparison:

| Node.js | Spring Boot |
|---|---|
| `app.listen(3000)` | `SpringApplication.run(...)` |
| `express()` app is created | Spring container is created |
| route handlers are registered manually | controllers are discovered automatically |
| modules are imported with `require()` | beans are registered via annotation scanning |

---

## 5. Spring Boot Startup Lifecycle

### Step 1: Java compiles code
The `.java` source files are compiled by the Java compiler into `.class` bytecode.

This is like Node.js when JavaScript is transpiled or executed by Node, but Java compiles to a bytecode format before it runs on the JVM.

### Step 2: Maven builds project
Maven reads `pom.xml` and resolves dependencies.

This is the Java equivalent of `npm install` and package management in Node.js.

### Step 3: project packaged
The project is packaged as:
- `.jar` (most common for Spring Boot)
- or `.war` if configured

In Node.js, this is similar to building a bundled app or creating a deployable package, but with Java’s jar packaging model.

### Step 4: Spring Boot runs
The `main` method triggers Spring Boot startup.

Example:
```java
public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
}
```

This is the Java equivalent of the Node.js application bootstrap file.

### Step 5: beans are created
Spring automatically creates beans based on annotations and configuration.

This is like Express registering handlers or modules in memory before requests arrive.

### Step 6: auto-configuration runs
Spring Boot configures defaults for:
- embedded server
- database support
- security
- web MVC
- JPA
- etc.

In Node.js, this is similar to framework defaults and middleware setup, but much more opinionated and automatic.

### Step 7: server accepts requests
Embedded Tomcat listens on a port like `8080` and handles HTTP traffic.

Equivalent to:
```js
app.listen(3000, () => console.log('Server running on 3000'));
```

---

## 6. What is Compilation in Java / Maven?

Compilation is the process of converting Java source files into bytecode which the JVM can execute.

Typical flow:
- Java source (`.java`)
- Java compiler
- bytecode (`.class`)
- packaged artifact (`.jar`)

Maven lifecycle:
- `validate`
- `compile`
- `test`
- `package`
- `verify`
- `install`
- `deploy`

Example command:
```bash
mvn clean compile
mvn test
mvn package
```

The `compile` phase checks code syntax and compiles classes.
The `package` phase creates a JAR/WAR file.

Node.js comparison:
- Node.js does not compile JavaScript into bytecode in the same way
- Node.js directly executes JavaScript using the V8 engine
- Java compiles into `.class` files before runtime, then JVM runs them

This is one reason Java apps can feel more formal and structured than Node.js apps.

---

## 7. Dependency Injection in Detail

Spring uses DI heavily.

### Constructor Injection (preferred)
```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Why preferred:
- immutable
- easier testing
- explicit dependencies
- no field mutation

### Setter Injection
```java
@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### Field Injection
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

This is simple but less recommended because of testability and immutability concerns.

---

## 8. Servlet and Spring Boot

A Servlet is a Java class that handles HTTP requests and responses.

In traditional Java web apps:
- a servlet container such as Tomcat/Jetty manages servlet lifecycle
- servlet receives HTTP requests
- servlet delegates processing to application logic

Spring Boot uses an embedded servlet container by default:
- Tomcat is embedded in the app
- no external application server is needed

Think of a servlet like a Node.js request handler running in the server runtime.

Example Node.js comparison:
```js
app.get('/users', (req, res) => {
  res.send('Users');
});
```

Equivalent idea in Spring:
```java
@GetMapping("/users")
public String getUsers() {
    return "Users";
}
```

Behind the scenes, the servlet container and `DispatcherServlet` route the request to your controller.

### Request flow
```text
Client
  ↓
HTTP request
  ↓
Embedded Tomcat
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository / Database
  ↓
Response
```

`DispatcherServlet` is the central Spring MVC servlet that routes requests to controller methods.

---

## 9. DispatcherServlet
`DispatcherServlet` is a front controller in Spring MVC.

Responsibilities:
- receives incoming HTTP requests
- identifies the correct controller
- invokes the handler method
- returns the response

It is part of the servlet-based architecture behind Spring Boot web applications.

Node.js analogy:
- In Express, the framework itself receives the HTTP request and calls the relevant route handler
- In Spring MVC, `DispatcherServlet` does a similar job: it receives all requests and routes them to the correct controller method

This is why Spring is sometimes described as a request-dispatching framework.

---

## 10. Controller, Service, Repository Pattern

This is a common layered architecture.

### Controller
Handles incoming HTTP requests.

```java
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String getUsers() {
        return userService.getUsers();
    }
}
```

### Service
Contains business logic.

```java
@Service
public class UserService {
    public String getUsers() {
        return "User list";
    }
}
```

### Repository
Data access layer.

```java
@Repository
public class UserRepository {
    public String findAllUsers() {
        return "data from database";
    }
}
```

This separates concerns cleanly.

---

## 11. Auto-Configuration

Spring Boot auto-configuration tries to configure common application settings automatically.

Examples:
- web server configuration
- database connection configuration
- security setup
- message converters
- actuator endpoints

This works by reading dependencies in `pom.xml` and then applying suitable defaults.

If you include:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Spring Boot configures a web application and embedded Tomcat automatically.

---

## 12. application.properties / application.yml

Configuration files store settings like:
- server port
- datasource URL
- logging level
- application name
- profiles

Example:
```properties
server.port=8080
spring.application.name=spring-boot-lab
```

YAML version:
```yaml
server:
  port: 8080

spring:
  application:
    name: spring-boot-lab
```

Node.js analogy:
- In Node.js, configuration often lives in `.env` or a config file
- In Spring Boot, configuration is commonly stored in `application.properties` or `application.yml`

---

## 13. Spring Boot Project Structure

A standard Spring Boot project looks like this:

```text
spring-boot-lab/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── demo/
│   │   │               ├── DemoApplication.java
│   │   │               ├── controller/
│   │   │               │   └── UserController.java
│   │   │               ├── service/
│   │   │               │   └── UserService.java
│   │   │               ├── repository/
│   │   │               │   └── UserRepository.java
│   │   │               └── model/
│   │   │                   └── User.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application.yml
│   │       └── static/
│   │           └── css/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── demo/
│                       └── DemoApplicationTests.java
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
├── README.md
└── target/
```

---

## 14. Project Structure in Tabular Form

| Path / File | Purpose | Explanation |
|---|---|---|
| `pom.xml` | Maven configuration | Declares project metadata, Java version, dependencies, and plugins |
| `src/main/java` | Java source code | Contains application classes such as controllers, services, repositories, models |
| `src/main/resources` | Configuration and static files | Stores `application.properties`, `application.yml`, templates, static assets |
| `src/test/java` | Unit and integration tests | Contains test files for validating app behavior |
| `DemoApplication.java` | Application entry point | Contains `main()` and `@SpringBootApplication` |
| `controller/` | Web request handling | Receives HTTP requests and calls services |
| `service/` | Business logic | Implements core logic for application features |
| `repository/` | Data access layer | Handles database interaction or persistence logic |
| `model/` | Data classes | Represents entities like `User`, `Product`, etc. |
| `resources/static` | Static files | CSS, JS, images, frontend assets |
| `resources/templates` | View templates | For server-rendered pages if using Thymeleaf |
| `target/` | Build output | Generated compiled classes and packaged JAR/WAR files |
| `mvnw` / `mvnw.cmd` | Maven wrapper | Runs Maven without globally installing it |
| `README.md` | Documentation | Explains project setup and usage |
| `.gitignore` | Git ignore list | Excludes generated or local files from git |

---

## 15. Typical Spring Boot Request Flow

```text
Request
  ↓
Tomcat (embedded)
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Database / External API
  ↓
Response returned to client
```

This is the typical flow for a REST-based Spring Boot application.

---

## 16. Bean Lifecycle

Spring manages the lifecycle of beans.

Typical lifecycle:
1. bean instantiated
2. dependencies injected
3. initialization methods executed
4. bean used in application
5. bean destroyed when context shuts down

You can use methods like:
- `@PostConstruct`
- `@PreDestroy`
- `InitializingBean`
- `DisposableBean`

---

## 17. Profiles

Spring profiles let you run the same app with different configurations.

Example:
```properties
spring.profiles.active=dev
```

Files:
- `application-dev.properties`
- `application-prod.properties`

This helps manage environment-specific settings.

In Node.js, a similar idea is using different environment files such as `.env.development` and `.env.production`, or choosing config based on `NODE_ENV`.

---

## 18. Actuator

Spring Boot Actuator adds production-ready monitoring endpoints.

Examples:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

Useful for:
- health monitoring
- metrics
- readiness checks
- system diagnostics

---

## 19. Common Spring Boot Starter Dependencies

These are common starter dependencies used in a Spring Boot app:

| Starter | Purpose |
|---|---|
| `spring-boot-starter-web` | Web application and REST APIs |
| `spring-boot-starter-data-jpa` | Database access using JPA |
| `spring-boot-starter-security` | Security features |
| `spring-boot-starter-test` | Unit/integration tests |
| `spring-boot-starter-thymeleaf` | HTML view templates |
| `spring-boot-starter-actuator` | Monitoring endpoints |

---

## 20. Major Differences: Spring Boot vs Node.js Backend

Here is a practical comparison for someone who already knows Node.js backend basics:

| Topic | Node.js (Express) | Spring Boot |
|---|---|---|
| Runtime | Node.js runtime | Java Virtual Machine (JVM) |
| Package manager | npm / package.json | Maven / pom.xml |
| Server startup | `app.listen(3000)` | `SpringApplication.run(...)` |
| Request handling | route handlers and middleware | controllers + `DispatcherServlet` |
| Dependency management | `require()` / imports | Spring bean container + DI |
| Object creation | manual instantiation | container-managed beans |
| Configuration | `.env`, JSON config, JS modules | `application.properties` / `application.yml` |
| Request routing | Express router | `@GetMapping`, `@PostMapping` |
| Business layer | service functions / helpers | `@Service` classes |
| Data access | Prisma / Sequelize / custom queries | `@Repository` + JPA / JDBC |
| Framework style | minimal and flexible | opinionated and convention-based |
| Auto-configuration | not built-in | built-in Spring Boot auto config |
| Lifecycle management | you write your own | Spring manages bean lifecycle |

This table helps you map concepts you already know in Node.js to the corresponding Spring Boot concepts.

---

## 21. Summary

Spring Boot is a powerful framework that simplifies Java application development.

Key ideas:
- Spring Boot is built on Spring
- It reduces boilerplate configuration
- Spring manages beans through IoC and DI
- `@SpringBootApplication` starts the app
- Embedded Tomcat handles HTTP requests
- MVC controller receives requests
- Service handles logic
- Repository handles data access
- Maven builds and packages the application
- `pom.xml` controls dependencies and plugins

This makes Spring Boot ideal for:
- REST APIs
- web apps
- microservices
- backend systems
- enterprise Java applications

The biggest mental shift from Node.js is that Spring Boot is more framework-driven and container-managed. In Node.js, you usually wire modules manually; in Spring Boot, the framework does a lot of the wiring automatically.

---

## 22. Minimal Example

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }
}
```

Node.js equivalent:
```js
const express = require('express');
const app = express();

app.get('/hello', (req, res) => {
  res.send('Hello from Spring Boot!');
});

app.listen(3000, () => {
  console.log('Server running on port 3000');
});
```

Run:
```bash
mvn spring-boot:run
```

Then open:
```text
http://localhost:8080/hello
```

This will print:
```text
Hello from Spring Boot!
```

---

## 22. Final Note

A Spring Boot application is essentially a Java application running inside an embedded servlet container, with Spring controlling object creation, dependencies, and lifecycle. It uses Maven for project management and packaging, and the configuration is mostly driven by sensible defaults and annotation-based code.

If you understand:
- Java basics
- Maven
- Spring IoC
- Dependency Injection
- Spring Boot annotations
- servlet-based request flow

then you are already on the path to building real-world Spring Boot applications.

This README is intended as a practical beginner-to-intermediate guide to understanding the ecosystem and architecture of Spring Boot.
````