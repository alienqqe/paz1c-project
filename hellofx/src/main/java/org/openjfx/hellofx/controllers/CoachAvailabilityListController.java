package org.openjfx.hellofx.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.openjfx.hellofx.model.CoachAvailabilityRow;
import org.openjfx.hellofx.services.CoachAvailabilityService;
import org.openjfx.hellofx.services.CoachService;
import org.openjfx.hellofx.utils.AuthContext;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class CoachAvailabilityListController implements Initializable {

    @FXML
    private TableView<CoachAvailabilityRow> table;
    @FXML
    private TableColumn<CoachAvailabilityRow, String> startCol;
    @FXML
    private TableColumn<CoachAvailabilityRow, String> endCol;
    @FXML
    private TableColumn<CoachAvailabilityRow, String> noteCol;
    @FXML
    private TableColumn<CoachAvailabilityRow, String> actionCol;

    private final CoachAvailabilityService availabilityService = new CoachAvailabilityService();
    private final CoachService coachService = new CoachService();
    private ResourceBundle resources;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        startCol.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().start())));
        endCol.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().end())));
        noteCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().note()));
        actionCol.setCellFactory(col -> new TableCellWithDelete());
        loadData();
    }

    private void loadData() {
        Long coachId = resolveCoachId();
        if (coachId == null) {
            showAlert(Alert.AlertType.ERROR, get("availability.list.noCoach"));
            return;
        }
        try {
            table.getItems().setAll(availabilityService.listUpcomingForCoach(coachId));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, get("availability.list.loadError") + ": " + e.getMessage());
        }
    }

    private Long resolveCoachId() {
        if (AuthContext.getCurrentUser() == null) return null;
        Long coachId = AuthContext.getCurrentUser().coachId();
        if (coachId != null) return coachId;
        try {
            return coachService.findCoachIdForUser(AuthContext.getCurrentUser().username());
        } catch (SQLException e) {
            return null;
        }
    }

    private class TableCellWithDelete extends javafx.scene.control.TableCell<CoachAvailabilityRow, String> {
        private final Button deleteBtn = new Button(get("availability.list.delete"));

        TableCellWithDelete() {
            deleteBtn.setOnAction(e -> {
                CoachAvailabilityRow row = getTableView().getItems().get(getIndex());
                if (row == null || row.id() == null) {
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, get("availability.list.confirmDelete"), ButtonType.OK, ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        Long coachId = resolveCoachId();
                        if (coachId == null) {
                            showAlert(Alert.AlertType.ERROR, get("availability.list.noCoach"));
                            return;
                        }
                        try {
                            availabilityService.deleteAvailability(coachId, row.id());
                            loadData();
                        } catch (SQLException ex) {
                            showAlert(Alert.AlertType.ERROR, get("availability.list.deleteError") + ": " + ex.getMessage());
                        }
                    }
                });
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                setGraphic(deleteBtn);
            }
        }
    }

    private String formatDate(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(formatter);
    }

    @FXML
    private void closeWindow() {
        if (table != null && table.getScene() != null && table.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String get(String key) {
        return resources != null && resources.containsKey(key) ? resources.getString(key) : key;
    }
}
