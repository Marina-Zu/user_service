package school.faang.user_service.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.dto.recommendation.SkillOfferDto;
import school.faang.user_service.entity.recommendation.Recommendation;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.recommendation.RecommendationRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;
    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void create_shouldReturnDataValidationException() {
        Assert.assertThrows(DataValidationException.class, () -> recommendationService.create(new RecommendationDto(1L,
                2L,
                3L,
                null,
                null,
                LocalDateTime.now())));
    }

    @Test
    public void create_shouldReturnCannotBeEmptyMsg() {
        DataValidationException dataValidationException = Assert.assertThrows(DataValidationException.class,
                () -> recommendationService
                        .create(new RecommendationDto(1L,
                                2L,
                                3L,
                                null,
                                null,
                                LocalDateTime.now())));
        String expectedMessage = "recommendation cannot be empty";
        String actualMessage = dataValidationException.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void create_shouldReturnEarlyThan6MonthsMsg() {
        RecommendationDto recommendationDto = new RecommendationDto();
        recommendationDto.setAuthorId(1L);
        recommendationDto.setReceiverId(2L);
        recommendationDto.setContent("content");

        Recommendation lastRecommendation = new Recommendation();
        lastRecommendation.setCreatedAt(LocalDateTime.now());

        when(recommendationRepository.findFirstByAuthorIdAndReceiverIdOrderByCreatedAtDesc(
                recommendationDto.getAuthorId(),
                recommendationDto.getReceiverId()))
                .thenReturn(Optional.of(lastRecommendation));

        DataValidationException dataValidationException = assertThrows(
                DataValidationException.class,
                () -> recommendationService.create(recommendationDto));

        String expectedMessage = "the recommendation can be given only after 6 months!";
        String actualMessage = dataValidationException.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void create_shouldReturnRecommendationId() {
        RecommendationDto recommendationDto = new RecommendationDto();
        recommendationDto.setAuthorId(anyLong());
        recommendationDto.setReceiverId(anyLong());
        recommendationDto.setContent("content");
        recommendationDto.setSkillOffers(List.of(new SkillOfferDto[]{}));

        when(recommendationRepository
                .findFirstByAuthorIdAndReceiverIdOrderByCreatedAtDesc(recommendationDto.getAuthorId(),
                        recommendationDto.getReceiverId()))
                .thenReturn(Optional.empty());

        Long expectedId = 10L;

        when(recommendationRepository.create(recommendationDto.getAuthorId(),
                recommendationDto.getReceiverId(),
                recommendationDto.getContent())).thenReturn(expectedId);

        Long resultId = recommendationService.create(recommendationDto);

        verify(recommendationRepository).create(recommendationDto.getAuthorId(),
                recommendationDto.getReceiverId(),
                recommendationDto.getContent());
        assertEquals(expectedId, resultId);

    }
}
