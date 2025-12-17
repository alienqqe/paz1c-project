package org.openjfx.hellofx.entities;

public record DiscountRule(
    long id,
        int visitsThreshold,
        int discountPercent
){
    // Expose bean-style getters so PropertyValueFactory can resolve properties
    public int getVisitsThreshold() {
        return visitsThreshold;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }
}
    
