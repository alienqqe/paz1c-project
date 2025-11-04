package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;

public record TrainingSession(Long id, Client client, Coach coach, LocalDateTime startDate){

}