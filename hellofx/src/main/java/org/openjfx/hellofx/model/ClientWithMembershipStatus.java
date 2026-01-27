package org.openjfx.hellofx.model;

/**
 * View model that combines client info with their current active membership, if any.
 */
public record ClientWithMembershipStatus(
    Long id,
    String name,
    String email,
    String phoneNumber,
    String membershipType,
    Integer remainingVisits
) {
}
