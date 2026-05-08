package com.openspec.usernameservice.api;

import java.util.List;

public record ErrorResponse(String code, String message, List<String> details) {}
