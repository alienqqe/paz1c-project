package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;

import org.openjfx.hellofx.dao.CoachDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.entities.Coach;

public class CoachService {
    private final CoachDAO coachDAO = DaoFactory.coaches();

    public List<Coach> searchCoaches(String query) throws SQLException {
        return coachDAO.searchCoaches(query);
    }

    public Long findCoachIdForUser(String username) throws SQLException {
        return coachDAO.findCoachIdForUser(username);
    }

    public Long addCoach(Coach coach) throws SQLException {
        return coachDAO.addCoach(coach);
    }
}
