package school.faang.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapperImpl;
import school.faang.user_service.repository.mentorship.MentorshipRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MentorshipServiceTest {
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Spy
    private UserMapperImpl userMapper;
    @InjectMocks
    private MentorshipService mentorshipService;
    private User user1;
    private User user2;
    private UserDto userDto;
    private List<User> users3;
    private List<UserDto> users2;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder().id(1L).build();
        user1 = User.builder().id(1L).build();
        users2 = List.of(userDto);
        users3 = List.of(user1);
        user2 = User.builder().id(2L).mentees(users3).build();
    }

    @Test
    @DisplayName("Checking for User availability")
    void testGetMentees_ShouldFindsUserById() {
        assertThrows(IllegalArgumentException.class,
                () -> mentorshipService.getMentees(1L));
    }

    @Test
    @DisplayName("Getting user's mentees")
    void testGetMentees_ShouldReturnsListOfUserDto() {
        when(mentorshipRepository.findById(2L))
                .thenReturn(Optional.of(user2));
        assertEquals(users2, mentorshipService.getMentees(2L));
    }
}