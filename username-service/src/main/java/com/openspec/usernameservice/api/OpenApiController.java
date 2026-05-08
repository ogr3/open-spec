package com.openspec.usernameservice.api;

import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v3")
public class OpenApiController {

    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> docs() throws IOException {
        ClassPathResource resource = new ClassPathResource("openapi/openapi.json");
        byte[] content = resource.getInputStream().readAllBytes();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(content);
    }
}
