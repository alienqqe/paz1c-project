package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.openjfx.hellofx.entities.Specialization;

public record Coach(Long id, String name, String email, String phoneNumber,
                    Map<LocalDateTime, TrainingSession> upcomingTrainings,
                    Set<Specialization> specializations){

}
