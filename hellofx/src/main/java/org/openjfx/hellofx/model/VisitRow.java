package org.openjfx.hellofx.model;

import java.time.LocalDateTime;

/**
 * Simple view model row for visit history table.
 */
public record VisitRow(
    Long id,
    String clientName,
    String clientEmail,
    String membershipType,
    LocalDateTime checkIn
) { }
