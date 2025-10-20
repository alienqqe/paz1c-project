package org.openjfx.hellofx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssignMembershipController {

    @FXML
    private Label clientNameLabel;
    @FXML
    private ChoiceBox<String> membershipTypeChoice;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private TextField durationField;
    @FXML
    private TextField priceField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        membershipTypeChoice.getItems().addAll("Monthly", "Yearly", "Weekly", "10 Visits");
    }

    @FXML
    private void onConfirm() {
        String type = membershipTypeChoice.getValue();
        String duration = durationField.getText();
        String price = priceField.getText();
        // Here you’d save membership to DB or client object
        System.out.println("Assigned " + type + " membership, duration: " + duration + ", price: " + price);
    }

    @FXML
    private void onCancel() {
        // Close window
        cancelButton.getScene().getWindow().hide();
    }

    public void setClientName(String name) {
        clientNameLabel.setText(name);
    }
}
