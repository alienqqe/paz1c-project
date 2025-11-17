package org.openjfx.hellofx.entities;

import java.time.LocalDateTime;

public record Visit( 
    Long id,
    Long clientId,
    Long membershipId, 
    LocalDateTime checkIn
    ){

}
