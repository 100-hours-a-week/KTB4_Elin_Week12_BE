package community.api.controller;

import java.time.Instant;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            Integer result =
                    jdbcTemplate.queryForObject(
                            "SELECT 1", Integer.class);

            if (result == null || result != 1) {
                throw new IllegalStateException(
                        "Database health check failed"
                );
            }
            return ResponseEntity.ok(
                    Map.of(
                            "status", "UP",
                            "database", "UP",
                            "timestamp", Instant.now().toString()
                    )
            );
        } catch (Exception exception) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(
                    Map.of(
                            "status", "DOWN",
                            "database", "DOWN",
                            "timestamp", Instant.now().toString()
                    )
            ); 
        }
    }
}
