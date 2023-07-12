package school.faang.user_service.service.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.event.EventMapper;
import school.faang.user_service.repository.event.EventRepository;
import school.faang.user_service.validator.EventValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    EventDto eventDto;
    EventDto eventDtoForUpdate;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventValidator eventValidator;
    @Mock
    private EventMapper eventMapper;
    @InjectMocks
    private EventService eventService;

    @BeforeEach
    public void init() {
        eventDto = new EventDto(1L, "Cool new Event", LocalDateTime.now(), LocalDateTime.now(),
                0L, "hfgh", new ArrayList<>(), "location", 1);
        eventDtoForUpdate = new EventDto(2L, "Event 1", LocalDateTime.now(), LocalDateTime.now(),
                0L, "Description", new ArrayList<>(), "location", 1);
    }

    @Test
    public void testCreateEvent() {
        eventService.createEvent(eventDto);
        Mockito.verify(eventRepository, Mockito.times(1)).save(eventMapper.toEntity(eventDto));
    }

    @Test
    public void testSkillsValidation() {
        eventValidator.checkIfUserHasSkillsRequired(eventDto);
        Mockito.verify(eventValidator, Mockito.times(1)).checkIfUserHasSkillsRequired(eventDto);
    }

    @Test
    public void testDeleteEvent() {
        long id = 3L;
        var event = Event.builder().id(id).build();
        Mockito.when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        eventService.deleteEvent(id);
        Mockito.verify(eventRepository, Mockito.times(1)).deleteById(id);
    }

    @Test
    public void testDeleteEventThrowsException() {
        long id = -1L;
        Assertions.assertThrows(DataValidationException.class, () -> {
            eventService.deleteEvent(id);
        });
    }

    @Test
    public void testUpdateEvent() {
        var event = Event.builder().id(1L).title("New Event").build();
        Mockito.when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        Mockito.when(eventMapper.toDto(event)).thenReturn(eventDtoForUpdate);
        EventDto updatedEvent = eventService.updateEvent(1L, eventDto);
        assertEquals("Cool new Event", updatedEvent.getTitle());
    }
}