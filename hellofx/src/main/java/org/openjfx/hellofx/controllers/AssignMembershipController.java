package org.openjfx.hellofx.controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.DiscountRuleDAO;
import org.openjfx.hellofx.dao.MembershipDAO;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.entities.DiscountRule;
import org.openjfx.hellofx.entities.Membership;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AssignMembershipController {

    @FXML
    private Label clientNameLabel;
    @FXML
    private ChoiceBox<String> membershipTypeChoice;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private TextField priceField;
    @FXML
    private Button confirmButton;
    private Long clientId;
    @FXML
    private Button cancelButton;
    
    private Runnable onSaved;

    @FXML
    public void initialize() {
        membershipTypeChoice.getItems().addAll("Monthly", "Yearly", "Weekly", "Ten");
    }

    @FXML
    private void onConfirm() {
        String type = membershipTypeChoice.getValue();
        String price = priceField.getText();
        // Validate input
        if (type == null || type.isEmpty()) {
            showAlert("Please select a membership type.");
            return;
        }

        DiscountRuleDAO discountRuleDAO = DaoFactory.discountRules();
        VisitDAO visitDao = DaoFactory.visits();

        int visitCount = visitDao.countVisitsForClient(clientId);

        double priceVal = 0.0;
        Optional<DiscountRule> bestDiscount = discountRuleDAO.bestRuleForVisits(visitCount);
        int appliedPercent = 0;
        double basePrice = 0.0;
        try {
            if (price != null && !price.isBlank()) {
                priceVal = Double.parseDouble(price);
                basePrice = priceVal;
                if (bestDiscount.isPresent()) {
                    appliedPercent = bestDiscount.get().getDiscountPercent();
                    priceVal = priceVal * (100 - appliedPercent) / 100.0;
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid price. Enter a valid number.");
            return;
        }

        LocalDate start = (startDatePicker.getValue() != null) ? startDatePicker.getValue() : LocalDate.now();
        LocalDate expires;
        Membership.MembershipType enumType;

         if ("Ten".equalsIgnoreCase(type) || "10 Visits".equalsIgnoreCase(type)) {
             enumType = Membership.MembershipType.Ten;
             // default expiry window for 10 visits
             expires = start.plusMonths(3);
         } else {
            try {
                enumType = Membership.MembershipType.valueOf(type);
            } catch (IllegalArgumentException ex) {
                enumType = Membership.MembershipType.Monthly;
            }

            switch (enumType) {
                case Monthly -> expires = start.plusMonths(1);
                case Yearly -> expires = start.plusYears(1);
                case Weekly -> expires = start.plusWeeks(1);
                default -> expires = start.plusMonths(1);
            }
        }

      
        int visits = enumType == Membership.MembershipType.Ten ? 10 : 0;
        Membership membership = new Membership(null, start, expires, priceVal, enumType, clientId, visits);
        MembershipDAO dao = DaoFactory.memberships();
        try {
            dao.addMembership(membership);
            System.out.println("Assigned " + type + " membership to client id " + clientId);
            if (appliedPercent > 0) {
                showInfo(
                    "Discount applied: " + appliedPercent + "% off (visits so far: " + visitCount + ").\n" +
                    "Original price: " + basePrice + " -> Final price: " + priceVal
                );
            } else {
                showInfo("No discount rule matched. Visits so far: " + visitCount + ".");
            }
            if (onSaved != null) {
                onSaved.run();
            }
            // close window
            confirmButton.getScene().getWindow().hide();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to save membership: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        // Close window
        cancelButton.getScene().getWindow().hide();
    }

    public void setClientName(String name) {
        clientNameLabel.setText(name);
    }

    public void setClient(Long id, String name) {
        this.clientId = id;
        setClientName(name);
    }
    
    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
