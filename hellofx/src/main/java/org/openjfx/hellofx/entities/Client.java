package org.openjfx.hellofx.entities;


public record Client(Long id, String name, boolean isActiveMember, boolean isInTheGym, String email, String phoneNumber){
    
}