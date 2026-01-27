package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.openjfx.hellofx.dao.CoachAvailabilityDAO;
import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.model.AvailabilitySlot;
import org.openjfx.hellofx.model.CoachAvailabilityRow;

public class CoachAvailabilityService {
    private final CoachAvailabilityDAO availabilityDAO = DaoFactory.coachAvailability();

    public boolean hasOverlap(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        return availabilityDAO.hasOverlap(coachId, start, end);
    }

    public void addAvailability(Long coachId, LocalDateTime start, LocalDateTime end, String note) throws SQLException {
        availabilityDAO.addAvailability(coachId, start, end, note);
    }

    public boolean isWithinAvailability(Long coachId, LocalDateTime start, LocalDateTime end) throws SQLException {
        return availabilityDAO.isWithinAvailability(coachId, start, end);
    }

    public void consumeAvailability(Long coachId, AvailabilitySlot slot, LocalDateTime start, LocalDateTime end) throws SQLException {
        availabilityDAO.consumeAvailability(coachId, slot, start, end);
    }

    public void deleteExpired() throws SQLException {
        availabilityDAO.deleteExpired();
    }

    public List<AvailabilitySlot> getAvailabilityForDate(Long coachId, LocalDate date) throws SQLException {
        return availabilityDAO.getAvailabilityForDate(coachId, date);
    }

    public List<CoachAvailabilityRow> listUpcomingForCoach(Long coachId) throws SQLException {
        return availabilityDAO.listUpcomingForCoach(coachId);
    }

    public void deleteAvailability(Long coachId, Long availabilityId) throws SQLException {
        availabilityDAO.deleteAvailability(coachId, availabilityId);
    }
}
