package org.openjfx.hellofx.services;

import java.sql.SQLException;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.MembershipDAO;
import org.openjfx.hellofx.entities.Membership;

public class MembershipService {
    private final MembershipDAO membershipDAO = DaoFactory.memberships();

    public boolean hasActiveMembership(Long clientId) throws SQLException {
        return membershipDAO.hasActiveMembership(clientId);
    }

    public void addMembership(Membership membership) throws SQLException {
        membershipDAO.addMembership(membership);
    }

    public String getCurrentMembershipType(Long clientId) throws SQLException {
        return membershipDAO.getCurrentMembershipType(clientId);
    }

    public Integer getRemainingVisits(Long clientId) throws SQLException {
        return membershipDAO.getRemainingVisits(clientId);
    }

    public void removeByHolderId(Long clientId) throws SQLException {
        membershipDAO.removeByHolderId(clientId);
    }
}
