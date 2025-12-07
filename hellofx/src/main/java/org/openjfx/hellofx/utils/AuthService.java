package org.openjfx.hellofx.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.openjfx.hellofx.dao.UserDAO;
import org.openjfx.hellofx.entities.User;
import org.openjfx.hellofx.dao.CoachDAO;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final CoachDAO coachDAO = new CoachDAO();

   
    public void ensureDefaultAdmin() throws SQLException {
        if (userDAO.countUsers() == 0) {
            userDAO.createUser("admin", "admin123", "ADMIN", null);
        }
    }

    public boolean login(String username, String password) throws SQLException {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.passwordHash())) {
                // If coach user missing coachId, try to resolve automatically and persist it.
                if ("COACH".equalsIgnoreCase(user.role()) && user.coachId() == null) {
                    Long coachId = coachDAO.findCoachIdForUser(user.username());
                    if (coachId != null) {
                        userDAO.updateCoachId(user.id(), coachId);
                        user = new User(user.id(), user.username(), user.passwordHash(), user.role(), coachId);
                    }
                }
                AuthContext.setCurrentUser(user);
                return true;
            }
        }
        return false;
    }

    public void logout() {
        AuthContext.clear();
    }

    public void createUser(String username, String rawPassword, String role, Long coachId) throws SQLException {
        userDAO.createUser(username, rawPassword, role, coachId);
    }

   
    public boolean changePassword(Long userId, String currentPassword, String newPassword) throws SQLException {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (!BCrypt.checkpw(currentPassword, user.passwordHash())) {
            return false;
        }
        userDAO.updatePassword(userId, newPassword);
        
        Optional<User> refreshed = userDAO.findById(userId);
        refreshed.ifPresent(AuthContext::setCurrentUser);
        return true;
    }
}
