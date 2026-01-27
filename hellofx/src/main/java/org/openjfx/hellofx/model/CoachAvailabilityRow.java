package org.openjfx.hellofx.model;

import java.time.LocalDateTime;

public record CoachAvailabilityRow(
    Long id,
    LocalDateTime start,
    LocalDateTime end,
    String note
) { }
