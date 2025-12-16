package org.openjfx.hellofx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.openjfx.hellofx.dao.VisitDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.openjfx.hellofx.utils.Database;

public class VisitHistoryController {

    @FXML private TableView<VisitRow> table;
    @FXML private TableColumn<VisitRow, String> clientCol;
    @FXML private TableColumn<VisitRow, String> emailCol;
    @FXML private TableColumn<VisitRow, String> membershipCol;
    @FXML private TableColumn<VisitRow, LocalDateTime> checkInCol;
    @FXML private TextField searchField;

    private final VisitDAO visitDAO = new VisitDAO();

    @FXML
    public void initialize() {
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("clientEmail"));
        membershipCol.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        loadVisits(null);
    }

    private void loadVisits(String filter) {
        ObservableList<VisitRow> rows = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection();
             ResultSet rs = (filter == null || filter.isBlank())
                 ? visitDAO.getRecentVisits(conn, 200)
                 : visitDAO.getRecentVisitsForClient(conn, filter.trim(), 200)) {
            while (rs.next()) {
                rows.add(new VisitRow(
                    rs.getLong("id"),
                    rs.getString("client_name"),
                    rs.getString("client_email"),
                    rs.getString("membership_type"),
                    rs.getTimestamp("check_in").toLocalDateTime()
                ));
            }
            table.setItems(rows);
        } catch (SQLException e) {
            e.printStackTrace();
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

    public static class VisitRow {
        private final Long id;
        private final String clientName;
        private final String clientEmail;
        private final String membershipType;
        private final LocalDateTime checkIn;

        public VisitRow(Long id, String clientName, String clientEmail, String membershipType, LocalDateTime checkIn) {
            this.id = id;
            this.clientName = clientName;
            this.clientEmail = clientEmail;
            this.membershipType = membershipType;
            this.checkIn = checkIn;
        }

        public Long getId() { return id; }
        public String getClientName() { return clientName; }
        public String getClientEmail() { return clientEmail; }
        public String getMembershipType() { return membershipType; }
        public LocalDateTime getCheckIn() { return checkIn; }
    }
}
