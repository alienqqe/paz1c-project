package org.openjfx.hellofx.model;

import java.time.LocalDateTime;

public record AvailabilitySlot(
    LocalDateTime start,
    LocalDateTime end,
    String note
) {}
