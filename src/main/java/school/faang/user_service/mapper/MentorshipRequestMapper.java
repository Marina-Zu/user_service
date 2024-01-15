package school.faang.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.dto.RejectionDto;
import school.faang.user_service.entity.MentorshipRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MentorshipRequestMapper {
    MentorshipRequestDto toMentorshipRequestDto(MentorshipRequest mentorshipRequest);
    MentorshipRequest MentorshipRequestToEntity(MentorshipRequestDto mentorshipRequestDto);

    RejectionDto toRejectionDto(MentorshipRequest mentorshipRequest);
    MentorshipRequest RejectionDtoToEntity(RejectionDto rejectionDto);
}
