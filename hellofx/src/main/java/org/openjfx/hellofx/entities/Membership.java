package org.openjfx.hellofx.entities;

import java.time.LocalDate;



public record Membership(Long id, LocalDate startDate, LocalDate expiresAt, double price, MembershipType Type, Long idOfHolder ){

    public enum MembershipType{
        Monthly, Yearly, Weekly, Ten
    }
}
