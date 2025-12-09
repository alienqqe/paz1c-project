package org.openjfx.hellofx.model;

import java.time.LocalTime;
import java.time.DayOfWeek;



public record WeeklySession(
    Long id,
    String coachName,
    String clientName,
    DayOfWeek day,
    LocalTime start,
    LocalTime end,
    String title
) {}
