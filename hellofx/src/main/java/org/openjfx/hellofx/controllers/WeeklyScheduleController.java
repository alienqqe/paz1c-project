package org.openjfx.hellofx.controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.model.WeeklySession;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class WeeklyScheduleController {
    @FXML private TableView<WeeklySession> table;
    @FXML private TableColumn<WeeklySession, String> coachCol;
    @FXML private TableColumn<WeeklySession, String> clientCol;
    @FXML private TableColumn<WeeklySession, String> dayCol;
    @FXML private TableColumn<WeeklySession, String> startCol;
    @FXML private TableColumn<WeeklySession, String> endCol;
    @FXML private TableColumn<WeeklySession, String> titleCol;

    private final TimetableDAO coachDao = new TimetableDAO();

    @FXML
    public void initialize() {
        coachCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().coachName()));
        clientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));
        dayCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().day().toString()));
        startCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().start().toString()));
        endCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().end().toString()));
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title()));
        loadCurrentWeek();
    }

    private void loadCurrentWeek() {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        try {
            table.getItems().setAll(coachDao.getWeeklySessions(weekStart));
        } catch (SQLException e) {
            // show alert or log
        }
    }
}
