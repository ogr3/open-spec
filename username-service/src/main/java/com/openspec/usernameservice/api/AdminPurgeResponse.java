package com.openspec.usernameservice.api;

public record AdminPurgeResponse(int deletedCount, int olderThanDays) {}
