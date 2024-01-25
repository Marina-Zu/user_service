package school.faang.user_service.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Data;
import school.faang.user_service.entity.goal.GoalStatus;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoalDto {
    private Long id;

    private String description;

    private Long parentId;

    private String title;

    private GoalStatus status;

    private List<Long> skillIds;
}
