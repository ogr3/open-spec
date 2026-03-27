package com.openspec.usernameservice.api;

import com.openspec.usernameservice.service.HandleAllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usernames")
public class HandleController {

    private final HandleAllocationService allocationService;

    public HandleController(HandleAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    public ResponseEntity<HandleResponse> reserve(@Valid @RequestBody HandleRequest request) {
        HandleResponse response = allocationService.allocate(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
