package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;


public record TrainingSession(
    Long id,
    Long clientId,
    Long coachId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String title
) {}
