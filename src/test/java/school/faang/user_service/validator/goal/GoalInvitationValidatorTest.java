package school.faang.user_service.validator.goal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.exception.goal.DataValidationException;
import school.faang.user_service.exception.goal.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Bulgakov
 */
@ExtendWith(MockitoExtension.class)
public class GoalInvitationValidatorTest {
    private GoalInvitationValidator goalInvitationValidator;

    @BeforeEach
    public void setup() {
        goalInvitationValidator = new GoalInvitationValidator();
    }

    @Test
    public void testCheckUser_InviterAndInvitedAreSame() {
        long inviterId = 1L;
        long invitedId = 1L;

        assertThrows(EntityNotFoundException.class, () ->
                goalInvitationValidator.checkUser(inviterId, invitedId)
        );
    }

    @Test
    public void testCheckUser_InviterIdNotEqualsInvitedId_NoExceptionThrown() {
        long inviterId = 1L;
        long invitedId = 2L;

        assertDoesNotThrow(() ->
            goalInvitationValidator.checkUser(inviterId, invitedId));
    }

    @Test
    public void testValidateGoal_WhenInvitationStatusIsNotPending_ShouldThrowDataValidationException() {
        GoalInvitation goalInvitation = new GoalInvitation();
        User user = goalInvitation.getInvited();
        goalInvitation.setStatus(RequestStatus.ACCEPTED);

        assertThrows(DataValidationException.class, () -> {
            goalInvitationValidator.validateGoal(user, goalInvitation);
        });
    }

    @Test
    public void testValidateGoal_WhenUserAlreadyHasGoal_ShouldThrowDataValidationException() {
        Goal goal = new Goal();
        goal.setId(1L);

        List<Goal> userGoals = new ArrayList<>();
        userGoals.add(goal);

        User user = new User();
        user.setId(1L);
        user.setGoals(userGoals);

        GoalInvitation goalInvitation = new GoalInvitation();
        goalInvitation.setInvited(user);
        goalInvitation.setStatus(RequestStatus.PENDING);
        goalInvitation.setGoal(goal);

        assertThrows(DataValidationException.class, () ->
            goalInvitationValidator.validateGoal(user, goalInvitation));
    }

    @Test
    public void testCheckFilter_AllFieldsNull() {
        InvitationFilterDto filter = null;

        boolean result = goalInvitationValidator.checkFilter(filter);

        assertFalse(result);
    }

    @Test
    public void testCheckFilter_SomeFieldsNotNull() {
        InvitationFilterDto filter = new InvitationFilterDto();
        filter.setInviterNamePattern("John");
        filter.setInvitedId(1L);

        boolean result = goalInvitationValidator.checkFilter(filter);

        assertTrue(result);
    }
}