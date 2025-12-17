package org.openjfx.hellofx.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.DiscountRuleDAO;
import org.openjfx.hellofx.entities.DiscountRule;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DiscountRulesController implements Initializable {

    @FXML private TextField thresholdsField;
    @FXML private TextField discountsField;

    @FXML private Label messageLabel;

    @FXML private TableView<DiscountRule> rulesTable;
    @FXML private TableColumn<DiscountRule, Integer> thresholdCol;
    @FXML private TableColumn<DiscountRule, Integer> percentCol;

    private final DiscountRuleDAO discountRuleDAO = DaoFactory.discountRules();
    private ResourceBundle resources;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        thresholdCol.setCellValueFactory(cell ->
            new SimpleIntegerProperty(cell.getValue().visitsThreshold()).asObject());
        percentCol.setCellValueFactory(cell ->
            new SimpleIntegerProperty(cell.getValue().discountPercent()).asObject());
        refreshFromDb();
    }

    private void refreshFromDb() {
        var rules = discountRuleDAO.findAllOrdered();
        rulesTable.setItems(FXCollections.observableArrayList(rules));
        setInputsFromRules(rules);
    }

    private List<DiscountRule> parseRules() {
        String thresholdsRaw = thresholdsField.getText() == null ? "" : thresholdsField.getText();
        String discountsRaw = discountsField.getText() == null ? "" : discountsField.getText();

        int[] thresholds = toIntArray(thresholdsRaw);
        int[] percents = toIntArray(discountsRaw);

        if (thresholds.length != percents.length) {
            throw new IllegalArgumentException(get("discount.error.count"));
        }
        if (thresholds.length == 0) {
            return new ArrayList<>();
        }

        List<DiscountRule> parsed = new ArrayList<>();
        for (int i = 0; i < thresholds.length; i++) {
            int t = thresholds[i];
            int p = percents[i];
            if (t <= 0) throw new IllegalArgumentException(get("discount.error.thresholds"));
            if (p < 0 || p > 100) throw new IllegalArgumentException(get("discount.error.discounts"));
            parsed.add(new DiscountRule(0L, t, p));
        }
        parsed.sort(Comparator.comparingInt(DiscountRule::visitsThreshold));
        return parsed;
    }

    private int[] toIntArray(String raw) {
        if (raw.isBlank()) {
            return new int[0];
        }
        try {
            return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(get("discount.error.number"));
        }
    }

    @FXML
    private void onPreview() {
        try {
            List<DiscountRule> parsed = parseRules();
            rulesTable.setItems(FXCollections.observableArrayList(parsed));
            setMessage(get("discount.message.preview"), "-fx-text-fill: #444;");
        } catch (Exception ex) {
            setMessage(ex.getMessage(), "-fx-text-fill: red;");
        }
    }

    @FXML
    private void onSave() {
        try {
            List<DiscountRule> parsed = parseRules();
            discountRuleDAO.replaceAll(parsed);
            refreshFromDb();
            setMessage(String.format(get("discount.message.saved"), parsed.size()), "-fx-text-fill: green;");
        } catch (Exception ex) {
            setMessage(ex.getMessage(), "-fx-text-fill: red;");
        }
    }

    @FXML
    private void onCancel() {
        // close dialog/window if possible; otherwise just clear message
        if (messageLabel != null && messageLabel.getScene() != null) {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            if (stage != null) {
                stage.close();
                return;
            }
        }
        setMessage("", null);
    }

    private void setInputsFromRules(List<DiscountRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }
        if (thresholdsField != null) {
            String thresholds = rules.stream()
                .map(r -> Integer.toString(r.visitsThreshold()))
                .collect(Collectors.joining(", "));
            thresholdsField.setText(thresholds);
        }
        if (discountsField != null) {
            String discounts = rules.stream()
                .map(r -> Integer.toString(r.discountPercent()))
                .collect(Collectors.joining(", "));
            discountsField.setText(discounts);
        }
    }

    private void setMessage(String text, String style) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            if (style != null) {
                messageLabel.setStyle(style);
            }
        }
    }

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }
}
