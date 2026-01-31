package org.openjfx.hellofx.model;

// model that helps us to retrieve client and his membership easier
// using this model we dont need to use few queries to obtain client with his membership
// with this model we can just use 1 query that joins 2 tables
public record ClientWithMembershipStatus(
    Long id,
    String name,
    String email,
    String phoneNumber,
    String membershipType,
    Integer remainingVisits
) {
}
