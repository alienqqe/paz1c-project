package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.Set;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.SpecializationDAO;
import org.openjfx.hellofx.entities.Specialization;

public class SpecializationService {
    private final SpecializationDAO specializationDAO = DaoFactory.specializations();

    public Set<Specialization> getForCoach(Long coachId) throws SQLException {
        return specializationDAO.getSpecializationsForCoach(coachId);
    }

    public void setForCoach(Long coachId, Set<String> specializationNames) throws SQLException {
        specializationDAO.setSpecializationsForCoach(coachId, specializationNames);
    }
}
