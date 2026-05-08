package com.openspec.usernameservice.api;

import com.openspec.usernameservice.service.HandleAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usernames")
@Tag(name = "Public", description = "Public username allocation endpoints")
public class HandleController {

    private final HandleAllocationService allocationService;

    public HandleController(HandleAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    @Operation(
            summary = "Reserve a username",
            responses = {
                @ApiResponse(responseCode = "201", description = "Created"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Collision exhaustion",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(
                        responseCode = "422",
                        description = "All handles blocked",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(
                        responseCode = "503",
                        description = "Persistence error",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<HandleResponse> reserve(@Valid @RequestBody HandleRequest request) {
        HandleResponse response = allocationService.allocate(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
