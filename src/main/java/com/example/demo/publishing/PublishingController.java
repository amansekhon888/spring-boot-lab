package com.example.demo.publishing;

import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/publishing")
public class PublishingController {
    private final PublicationRepository repository;

    public PublishingController(PublicationRepository r) {
        repository = r;
    }

    public record ScheduleRequest(@NotNull Platform platform, @NotBlank @Size(max = 3000) String content,
            @NotNull Instant scheduledAt) {
    }

    @PostMapping("/jobs")
    public ResponseEntity<Map<String, Object>> schedule(
            @jakarta.validation.Valid @RequestBody ScheduleRequest request) {
        var job = repository.save(new ScheduledPublication(request.platform(), request.content(), request.scheduledAt()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", job.getId(), "status", job.getStatus(), "scheduledAt", job.getScheduledAt()));
    }

    @GetMapping("/jobs/{id}")
    public ScheduledPublication get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }
}
