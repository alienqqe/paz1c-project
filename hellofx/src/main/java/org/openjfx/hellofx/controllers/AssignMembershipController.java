package org.openjfx.hellofx.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.DiscountRuleDAO;
import org.openjfx.hellofx.dao.MembershipDAO;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.entities.DiscountRule;
import org.openjfx.hellofx.entities.Membership;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class AssignMembershipController implements Initializable {

    @FXML
    private Label clientNameLabel;
    @FXML
    private ChoiceBox<Membership.MembershipType> membershipTypeChoice;
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
    private ResourceBundle resources;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        this.resources = resources;
        membershipTypeChoice.getItems().addAll(
            Membership.MembershipType.Monthly,
            Membership.MembershipType.Yearly,
            Membership.MembershipType.Weekly,
            Membership.MembershipType.Ten
        );
        membershipTypeChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Membership.MembershipType type) {
                if (type == null) return "";
                return switch (type) {
                    case Ten -> get("membership.type.ten");
                    case Monthly -> get("membership.type.monthly");
                    case Weekly -> get("membership.type.weekly");
                    case Yearly -> get("membership.type.yearly");
                };
            }

            @Override
            public Membership.MembershipType fromString(String string) {
                return null;
            }
        });
        if (startDatePicker != null) {
            startDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        return;
                    }
                    if (item.isBefore(LocalDate.now())) {
                        setDisable(true);
                    }
                }
            });
        }
    }

    @FXML
    private void onConfirm() {
        Membership.MembershipType enumType = membershipTypeChoice.getValue();
        String price = priceField.getText();
        // Validate input
        if (enumType == null) {
            showAlert(get("assign.error.selectType"));
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
            showAlert(get("assign.error.price"));
            return;
        }

        LocalDate start = (startDatePicker.getValue() != null) ? startDatePicker.getValue() : LocalDate.now();
        if (start.isBefore(LocalDate.now())) {
            showAlert(get("assign.error.startPast"));
            return;
        }
        LocalDate expires;
        if (enumType == Membership.MembershipType.Ten) {
            // default expiry window for 10 visits
            expires = start.plusMonths(3);
        } else {
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
            System.out.println("Assigned membership to client id " + clientId);
            if (appliedPercent > 0) {
                showInfo(String.format(get("assign.discount.applied"), appliedPercent, visitCount, basePrice, priceVal));
            } else {
                showInfo(String.format(get("assign.discount.none"), visitCount));
            }
            if (onSaved != null) {
                onSaved.run();
            }
            // close window
            confirmButton.getScene().getWindow().hide();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(get("assign.error.save") + ": " + e.getMessage());
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

    private String get(String key) {
        return (resources != null && resources.containsKey(key)) ? resources.getString(key) : key;
    }
}
