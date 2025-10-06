package org.openjfx.hellofx.entities;

import java.time.LocalDate;

public record Client(Long id, boolean isActiveMember, boolean isInTheGym, String email, String phoneNumber, LocalDate[] visitHistory){
    
}