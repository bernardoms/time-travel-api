package com.bernardoms.timetravelapi.unit.service;

import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import com.bernardoms.timetravelapi.model.TimeTravel;
import com.bernardoms.timetravelapi.repository.TimeTravelRepository;
import com.bernardoms.timetravelapi.service.TimeTravelServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeTravelServiceImplUnitTest {

    @Mock
    private TimeTravelRepository timeTravelRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TimeTravelServiceImpl timeTravelService;

    @Test
    void should_save_throw_paradox_exception_when_saving_travel_that_exist_for_a_pgi_at_same_date_and_location() {
        when(timeTravelRepository
                .findByPgiAndDate("A1234", LocalDate.of(2020, 10, 10)))
                .thenReturn(Optional.of(TimeTravel.builder().pgi("A1234").place("London").date(LocalDate.of(2020, 10, 10))
                        .build()));
        var exception = assertThrows(ParadoxException.class, () -> {
            timeTravelService.saveTravel(TimeTravelDTO.builder().pgi("A1234").place("London").date(LocalDate.of(2020, 10, 10)).build());
            verify(timeTravelRepository, never()).save(any(TimeTravel.class));
        });
        assertEquals("Paradox detected! traveler with pgi A1234 already traveled to London at date 2020-10-10", exception.getMessage());
    }

    @Test
    void should_save_new_travel_and_return_saved_id() throws ParadoxException {
        var travelDTO = TimeTravelDTO.builder()
                .pgi("A1234")
                .place("London")
                .date(LocalDate.of(2020, 10, 10))
                .build();

        var travel = TimeTravel.builder()
                .pgi("A1234")
                .place("London")
                .date(LocalDate.of(2020, 10, 10))
                .id(new ObjectId("507f191e810c19729de860eb"))
                .build();

        when(timeTravelRepository.save(any(TimeTravel.class))).thenReturn(travel);

        when(modelMapper.map(travelDTO, TimeTravel.class)).thenReturn(travel);

        var savedTravelId = timeTravelService.saveTravel(travelDTO);
        assertEquals("507f191e810c19729de860eb", savedTravelId);
    }

    @Test
    void should_get_travel_if_exist() throws Exception {
        var travel = TimeTravel.builder()
                .pgi("A1234")
                .place("London")
                .date(LocalDate.of(2020, 10, 10))
                .id(new ObjectId("507f191e810c19729de860eb"))
                .build();

        var travelDTO = TimeTravelDTO.builder()
                .pgi("A1234")
                .place("London")
                .date(LocalDate.of(2020, 10, 10))
                .build();

        when(modelMapper.map(travel, TimeTravelDTO.class)).thenReturn(travelDTO);

        when(timeTravelRepository.findById(new ObjectId("507f191e810c19729de860eb"))).thenReturn(Optional.of(travel));

        var response = timeTravelService.getTravel(new ObjectId("507f191e810c19729de860eb"));

        assertEquals(travelDTO.getPgi(), response.getPgi());
        assertEquals(travelDTO.getPlace(), response.getPlace());
        assertEquals(travelDTO.getDate(), response.getDate());
    }

    @Test
    void should_throw_travel_not_found_exception_when_not_found_a_travel() {

        when(timeTravelRepository.findById(new ObjectId("507f191e810c19729de860eb"))).thenReturn(Optional.empty());

        var exception = assertThrows(TravelNotFoundException.class, () -> timeTravelService.getTravel(new ObjectId("507f191e810c19729de860eb")));

        assertEquals("Travel with id 507f191e810c19729de860eb not found!", exception.getMessage());
    }

    @Test
    void should_return_all_travels() {
        var travel1 = TimeTravel.builder()
                .pgi("A1234")
                .date(LocalDate.of(2020, 10, 10)).place("London")
                .build();

        var travel2 = TimeTravel.builder()
                .pgi("B1234")
                .date(LocalDate.of(2020, 10, 11))
                .place("Brazil").build();

        when(timeTravelRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(travel1, travel2)));
        Page<TimeTravelDTO> travels = timeTravelService.getTravels(Pageable.unpaged());

        assertEquals(2, travels.getTotalElements());

        assertEquals(1, travels.getTotalPages());

        assertEquals("London", travels.get().filter(t -> t.getPgi().equals("A1234")).findFirst().get().getPlace());

        assertEquals("Brazil", travels.get().filter(t -> t.getPgi().equals("B1234")).findFirst().get().getPlace());
    }

    @Test
    void should_delete_travel() {
        timeTravelService.deleteTravel(new ObjectId("507f191e810c19729de860eb"));
        verify(timeTravelRepository, times(1)).deleteById(new ObjectId("507f191e810c19729de860eb"));
    }
}
