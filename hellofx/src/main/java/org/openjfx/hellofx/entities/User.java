package org.openjfx.hellofx.entities;

public record User(
    Long id,
    String username,
    String passwordHash,
    String role,
    Long coachId
) {}
