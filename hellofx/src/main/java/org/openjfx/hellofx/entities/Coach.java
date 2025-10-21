package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record Coach(Long id, String name, String email, String phoneNumber, Map<LocalDateTime, TrainingSession> upcomingTrainings, Set<String> specializations){

}
