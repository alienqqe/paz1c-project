package org.openjfx.hellofx.utils;

import org.openjfx.hellofx.entities.User;

public final class AuthContext {
    private static User currentUser;

    private AuthContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.role());
    }

    public static boolean isCoach() {
        return currentUser != null && "COACH".equalsIgnoreCase(currentUser.role());
    }

    public static void clear() {
        currentUser = null;
    }
}
