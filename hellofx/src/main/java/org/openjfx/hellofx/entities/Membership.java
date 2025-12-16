package org.openjfx.hellofx.entities;

import java.time.LocalDate;



public record Membership(Long id, LocalDate startDate, LocalDate expiresAt, double price, MembershipType type, Long idOfHolder, int visitsRemaining ){

    public enum MembershipType{
        Monthly, Yearly, Weekly, Ten
    }
}
