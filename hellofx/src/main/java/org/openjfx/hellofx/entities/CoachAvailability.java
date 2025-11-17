package org.openjfx.hellofx.entities;

import java.time.LocalTime;

public record CoachAvailability(Long id, Long coachId , DayOfWeekCustom dayOfWeek , LocalTime startTime, LocalTime endTime) {
    public enum DayOfWeekCustom{
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }
}