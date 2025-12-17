package org.openjfx.hellofx.controllers;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;


import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.TimetableDAO;
import org.openjfx.hellofx.model.WeeklySession;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


public class WeeklyScheduleController {
    @FXML private TableView<WeeklySession> table;
    @FXML private TableColumn<WeeklySession, String> coachCol;
    @FXML private TableColumn<WeeklySession, String> clientCol;
    @FXML private TableColumn<WeeklySession, String> dayCol;
    @FXML private TableColumn<WeeklySession, String> startCol;
    @FXML private TableColumn<WeeklySession, String> endCol;
    @FXML private TableColumn<WeeklySession, String> titleCol;
    @FXML private TableColumn<WeeklySession, String> actionCol;

    private final TimetableDAO coachDao = DaoFactory.timetable();

    @FXML
    public void initialize() {
        coachCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().coachName()));
        clientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().clientName()));
        dayCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().day().toString()));
        startCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().start().toString()));
        endCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().end().toString()));
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title()));
        actionCol.setCellFactory(col -> new TableCellWithDelete());
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

    private class TableCellWithDelete extends javafx.scene.control.TableCell<WeeklySession, String> {
        private final Button deleteBtn = new Button("Delete");

        TableCellWithDelete() {
            deleteBtn.setOnAction(e -> {
                WeeklySession session = getTableView().getItems().get(getIndex());
                if (session == null || session.id() == null) {
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this session?", javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == javafx.scene.control.ButtonType.OK) {
                        try {
                            coachDao.deleteTrainingSession(session.id());
                            loadCurrentWeek();
                        } catch (SQLException ex) {
                            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete: " + ex.getMessage());
                            err.showAndWait();
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
}
