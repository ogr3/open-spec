package com.openspec.usernameservice.api;

import com.openspec.usernameservice.reservation.AdminReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/handles")
@Tag(name = "Admin", description = "Administrative handle mapping operations")
public class AdminController {

    private final AdminReservationService adminReservationService;

    public AdminController(final @NonNull AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping
    @Operation(
            summary = "List handle mappings",
            description = "Returns recent handle-to-email mappings ordered by creation time descending.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Recent mappings"),
                @ApiResponse(
                        responseCode = "503",
                        description = "Persistence error",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<List<AdminHandleReservationResponse>> list(
            @RequestParam(name = "limit", defaultValue = "100") int limit) {
        final List<AdminHandleReservationResponse> response = adminReservationService.listRecent(limit).stream()
                .map(reservation -> new AdminHandleReservationResponse(
                        reservation.getHandle(), reservation.getEmail(), reservation.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(
            summary = "Delete old mappings",
            description = "Deletes mappings older than the given age in days.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Purge result"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(
                        responseCode = "503",
                        description = "Persistence error",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<AdminPurgeResponse> deleteOldMappings(
            @RequestParam(name = "olderThanDays") int olderThanDays) {
        final int deletedCount = adminReservationService.deleteOlderThanDays(olderThanDays);
        return ResponseEntity.ok(new AdminPurgeResponse(deletedCount, olderThanDays));
    }
}
