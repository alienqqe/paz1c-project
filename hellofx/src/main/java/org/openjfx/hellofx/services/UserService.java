package org.openjfx.hellofx.services;

import java.sql.SQLException;
import java.util.Optional;

import org.openjfx.hellofx.dao.DaoFactory;
import org.openjfx.hellofx.dao.UserDAO;
import org.openjfx.hellofx.entities.User;

public class UserService {
    private final UserDAO userDAO = DaoFactory.users();

    public Optional<User> findByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
}
