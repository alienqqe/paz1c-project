package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;
import java.util.Map;

public record Coach(Long id, String name, String email, String phoneNumber, Map<LocalDateTime, TrainingSession> upcomingTrainings){

}