package org.openjfx.hellofx.controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.model.VisitRow;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VisitHistoryController implements Initializable {

    @FXML private TableView<VisitRow> table;
    @FXML private TableColumn<VisitRow, String> clientCol;
    @FXML private TableColumn<VisitRow, String> emailCol;
    @FXML private TableColumn<VisitRow, String> membershipCol;
    @FXML private TableColumn<VisitRow, LocalDateTime> checkInCol;
    @FXML private TextField searchField;

    private final VisitDAO visitDAO = DaoFactory.visits();
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        // we use observable list to make the table content reactive to the changes (so no refresh needed)
        ObservableList<VisitRow> rows = FXCollections.observableArrayList();
        try {
            List<VisitRow> result = (filter == null || filter.isBlank())
                ? visitDAO.getRecentVisits(200)
                : visitDAO.getRecentVisitsForClient(filter.trim(), 200);
            for (VisitRow v : result) {
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
            Alert alert = new Alert(Alert.AlertType.ERROR, get("visit.error.load") + ": " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void onSearch() {
        String filter = searchField != null ? searchField.getText() : null;
        loadVisits(filter);
    }

    private String get(String key) {
        return (resources != null && resources.containsKey(key)) ? resources.getString(key) : key;
    }

}
