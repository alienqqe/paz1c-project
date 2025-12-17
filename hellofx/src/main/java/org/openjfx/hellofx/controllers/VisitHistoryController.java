package org.openjfx.hellofx.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.dao.VisitDAO.VisitView;
import org.openjfx.hellofx.model.VisitRow;

import java.time.LocalDateTime;
import java.util.List;

public class VisitHistoryController {

    @FXML private TableView<VisitRow> table;
    @FXML private TableColumn<VisitRow, String> clientCol;
    @FXML private TableColumn<VisitRow, String> emailCol;
    @FXML private TableColumn<VisitRow, String> membershipCol;
    @FXML private TableColumn<VisitRow, LocalDateTime> checkInCol;
    @FXML private TextField searchField;

    private final VisitDAO visitDAO = DaoFactory.visits();

    @FXML
    public void initialize() {
        clientCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().clientName()));
        emailCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().clientEmail()));
        membershipCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().membershipType()));
        checkInCol.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().checkIn()));
        loadVisits(null);
    }

    private void loadVisits(String filter) {
        ObservableList<VisitRow> rows = FXCollections.observableArrayList();
        try {
            List<VisitView> result = (filter == null || filter.isBlank())
                ? visitDAO.getRecentVisits(200)
                : visitDAO.getRecentVisitsForClient(filter.trim(), 200);
            for (VisitView v : result) {
                rows.add(new VisitRow(
                    v.id(),
                    v.clientName(),
                    v.clientEmail(),
                    v.membershipType(),
                    v.checkIn()
                ));
            }
            table.setItems(rows);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load visit history: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void onSearch() {
        String filter = searchField != null ? searchField.getText() : null;
        loadVisits(filter);
    }

    @FXML
    void onClear() {
        if (searchField != null) {
            searchField.clear();
        }
        loadVisits(null);
    }

}
