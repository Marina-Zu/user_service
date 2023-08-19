package school.faang.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.config.executors.ExecutorsPull;
import school.faang.user_service.csv_parser.CsvToPerson.CsvToPerson;
import school.faang.user_service.dto.DeactivateResponseDto;
import school.faang.user_service.dto.subscription.UserDto;
import school.faang.user_service.dto.subscription.UserFilterDto;
import school.faang.user_service.dto.user.person_dto.UserPersonDto;
import school.faang.user_service.entity.Country;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.exception.DataValidException;
import school.faang.user_service.exception.DeactivationException;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.filter.user.UserFilter;
import school.faang.user_service.mapper.PersonToUserMapper;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.repository.CountryRepository;
import school.faang.user_service.repository.UserCheckRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.event.EventRepository;
import school.faang.user_service.repository.goal.GoalRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final CsvToPerson csvToPerson;
    private final PersonToUserMapper personToUserMapper;
    private final EventRepository eventRepository;
    private final GoalRepository goalRepository;
    private final MentorshipService mentorshipService;
    private final UserRepository userRepository;
    private final List<UserFilter> userFilters;
    private final UserMapper userMapper;
    private final CountryRepository countryRepository;
    private final UserCheckRepository userCheckRepository;

    private final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String lower = upper.toLowerCase(Locale.ROOT);
    private final String numbers = "0123456789";
    private final String set = upper + lower + numbers;

    @Autowired
    private ExecutorsPull executorsPull;

    @Value("${dicebear.url}")
    private String dicebearUrl;

    public UserDto getUser(long id) {
        User foundUser = userRepository.findById(id).orElseThrow(() -> {
            throw new UserNotFoundException("User with id " + id + " not found");
        });
        log.info("Return user with id: {}", foundUser.getId());
        return userMapper.toUserDto(foundUser);
    }

    public List<UserDto> getUsersByIds(List<Long> usersIds) {
        List<User> users = userRepository.findAllById(usersIds);
        log.info("Return list of users: {}", users);
        return userMapper.toUserListDto(users);
    }

    public void registerAnArrayOfUser(InputStream stream) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            try {
                String finalLine = line;
                executorsPull.pullUserService().getPull().execute(() -> saveUserStudent(finalLine));
            } catch (DataValidException e) {
                System.out.println(e.getMessage());
            }
        }
        reader.close();
    }

    private void saveUserStudent(String line) {
        Supplier<String> password = () -> {
            int size = new Random().nextInt(10, 20);
            char[] chars = new char[size];
            for (int i = 0; i < chars.length; i++) {
                chars[i] = set.charAt(new Random().nextInt(set.length()));
            }
            return new String(chars);
        };

        UserPersonDto personDto = csvToPerson.getPerson(line);
        User user = personToUserMapper.toUser(personDto);

        String personCountry = personDto.getContactInfo().getAddress().getCountry();
        List<User> users = userCheckRepository.findDistinctPeopleByUsernameOrEmailOrPhone(personDto.getFirstName(),
                personDto.getContactInfo().getEmail(), personDto.getContactInfo().getPhone());
        List<UserPersonDto> personDtos = users.stream().map(personToUserMapper::toUserPersonDto).toList();
        if (personDtos.contains(personDto)) {
            return;
        }

        user.setPassword(password.get());

        Map<String, Country> countryMap = new HashMap<>();
        countryRepository.findAll().forEach(country -> countryMap.put(country.getTitle(), country));
        if (countryMap.containsKey(personCountry)) {
            user.setCountry(countryMap.get(personCountry));
        } else {
            Country countrySave = new Country();
            countrySave.setTitle(personCountry.strip());
            countryRepository.save(countrySave);
        }

        userRepository.save(user);
    }

    public UserDto signup(UserDto userDto) {
        //TODO Нужно реализовать логику создания юзера
        User user = userRepository.findById(userDto.getId()).orElseThrow(() -> new UserNotFoundException("User is not found"));
        setDefaultAvatar(user);
        return userDto;
    }

    public List<UserDto> getPremiumUsers(UserFilterDto userFilterDto) {
        Stream<User> premiumUsers = userRepository.findPremiumUsers();

        premiumUsers = filter(userFilterDto, premiumUsers);
        return userMapper.toUserListDto(premiumUsers.toList());
    }

    @Transactional
    public DeactivateResponseDto deactivateUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DeactivationException("there is no user", userId));
        if (!user.isActive()) {
            throw new DeactivationException("The user has already been deactivated", userId);
        }
        List<Goal> goals = goalRepository.findGoalsByUserId(userId).toList();
        goals.stream().filter(goal -> goal.getUsers().size() == 1).forEach(goalRepository::delete);

        goals.forEach(goal -> deleteUser(goal, user));
        goalRepository.saveAll(goals);

        List<Event> eventList = eventRepository.findAllByUserId(userId);
        eventRepository.deleteAll(eventList.stream().filter(event -> event.getOwner().getId() == userId).toList());

        mentorshipService.cancelMentoring(user, goals);

        user.setActive(false);
        userRepository.save(user);
        return new DeactivateResponseDto("The user is deactivated", userId);
    }

    @Transactional
    public void banUser(long id) {
        userRepository.banUser(id);
    }

    private void deleteUser(Goal goal, User user) {
        List<User> users = new ArrayList<>(goal.getUsers());
        users.remove(user);
        goal.setUsers(users);
    }

    private Stream<User> filter(UserFilterDto userFilterDto, Stream<User> premiumUsers) {
        for (UserFilter filter : userFilters) {
            if (filter.isApplicable(userFilterDto)) {
                premiumUsers = filter.apply(premiumUsers, userFilterDto);
            }
        }
        return premiumUsers;
    }

    private void setDefaultAvatar(User user) {
        UserProfilePic userProfilePic = new UserProfilePic();
        userProfilePic.setFileId(dicebearUrl + user.getUsername() + "&scale=" + 130);
        userProfilePic.setSmallFileId(dicebearUrl + user.getUsername() + "&scale=" + 22);
        user.setUserProfilePic(userProfilePic);
    }
}
