package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.List;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.VisitDAO;
import org.openjfx.hellofx.model.VisitRow;

public class VisitService {
    private final VisitDAO visitDAO = DaoFactory.visits();

    public int countVisitsForClient(Long clientId) throws SQLException {
        return visitDAO.countVisitsForClient(clientId);
    }

    public boolean checkInClient(Long clientId) throws SQLException {
        return visitDAO.checkInClient(clientId);
    }

    public List<VisitRow> getRecentVisits(int limit) {
        return visitDAO.getRecentVisits(limit);
    }

    public List<VisitRow> getRecentVisitsForClient(String filter, int limit) {
        return visitDAO.getRecentVisitsForClient(filter, limit);
    }
}
