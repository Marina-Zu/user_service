package school.faang.user_service.service.goal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.goal.GoalDto;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.filters.goal.GoalFilter;
import school.faang.user_service.filters.goal.dto.GoalFilterDto;
import school.faang.user_service.mapper.GoalMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.goal.GoalRepository;
import school.faang.user_service.util.Message;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {
    private static final int MAX_ACTIVE_GOALS = 3;
    private final GoalRepository goalRepository;
    private final SkillRepository skillRepository;
    private final GoalMapper goalMapper;
    private final List<GoalFilter> goalFilters;

    @Transactional
    public GoalDto createGoal(GoalDto goal, Long userId) {
        int currentUserGoalNum = goalRepository.countActiveGoalsPerUser(userId);
        boolean allSkillsExist = goal.getSkills().stream()
                .allMatch(skill -> skillRepository.findByTitle(skill.toLowerCase()).isPresent());

        if (!allSkillsExist) {
            throw new IllegalArgumentException(Message.UNEXISTING_SKILLS);
        }
      
        if (currentUserGoalNum > MAX_ACTIVE_GOALS){
            throw new IllegalArgumentException(Message.TOO_MANY_GOALS);
        }

        goalRepository.save(goalMapper.goalToEntity(goal));

        return goal;
    }

    @Transactional
    public GoalDto updateGoal(GoalDto goalDto, Long userId) {
         Goal goal = goalRepository.findById(goalDto.getId())
               .orElseThrow(() -> new  IllegalArgumentException(
                     MessageFormat.format("Goal {0} not found", goalDto.getId())));

        goal.setTitle(goalDto.getTitle());
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        return goalMapper.goalToDto(goal);
    }

    @Transactional
    public void deleteGoal(Long goalId){
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(MessageFormat.format("Goal {0} not found", goalId)));

        goalRepository.delete(goal);
    }

    public List<GoalDto> getGoalsByUser(Long userId, GoalFilterDto goalFilterDto){
        List<Goal> goals = goalRepository.findGoalsByUserId(userId)
                        .peek(goal -> goal.setSkillsToAchieve(skillRepository.findSkillsByGoalId(goal.getId())))
                .toList();

        return applyFilter(goals, goalFilterDto);
    }

    private List<GoalDto> applyFilter(List<Goal> goals, GoalFilterDto goalFilterDto){
        List<GoalDto> list = goalFilters.stream()
                .filter(goalFilter -> goalFilter.isApplicable(goalFilterDto))
                .flatMap(goalFilter -> goalFilter.apply(goals.stream(), goalFilterDto))
                .map(goalMapper::goalToDto)
                .toList();
        return list;
    }

    private List<GoalDto> filterGoals(List<Goal> goals, GoalFilterDto goalFilterDto){
        List<GoalDto> filteredGoals = goals.stream()
                .filter(goal -> goal.getTitle().equals(goalFilterDto.getTitle()) && goal.getStatus().equals(goalFilterDto.getStatus()))
                .map(goalMapper::goalToDto)
                .toList();

        return filteredGoals;
    }
}