package org.openjfx.hellofx.dao;

/**
 * Simple factory/singleton provider for DAOs so controllers have a single place to obtain them.
 */
public final class DaoFactory {
    private static final UserDAO USER_DAO = new UserDAO();
    private static final ClientDAO CLIENT_DAO = new ClientDAO();
    private static final CoachDAO COACH_DAO = new CoachDAO();
    private static final MembershipDAO MEMBERSHIP_DAO = new MembershipDAO();
    private static final VisitDAO VISIT_DAO = new VisitDAO();
    private static final TimetableDAO TIMETABLE_DAO = new TimetableDAO();
    private static final CoachAvailabilityDAO COACH_AVAILABILITY_DAO = new CoachAvailabilityDAO();
    private static final DiscountRuleDAO DISCOUNT_RULE_DAO = new DiscountRuleDAO();
    private static final SpecializationDAO SPECIALIZATION_DAO = new SpecializationDAO();

    private DaoFactory() {
    }

    public static UserDAO users() { return USER_DAO; }
    public static ClientDAO clients() { return CLIENT_DAO; }
    public static CoachDAO coaches() { return COACH_DAO; }
    public static MembershipDAO memberships() { return MEMBERSHIP_DAO; }
    public static VisitDAO visits() { return VISIT_DAO; }
    public static TimetableDAO timetable() { return TIMETABLE_DAO; }
    public static CoachAvailabilityDAO coachAvailability() { return COACH_AVAILABILITY_DAO; }
    public static DiscountRuleDAO discountRules() { return DISCOUNT_RULE_DAO; }
    public static SpecializationDAO specializations() { return SPECIALIZATION_DAO; }
}
