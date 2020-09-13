package com.bernardoms.timetravelapi.unit.controller;

import com.bernardoms.timetravelapi.controller.ExceptionController;
import com.bernardoms.timetravelapi.controller.TimeTravelController;
import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import com.bernardoms.timetravelapi.service.TimeTravelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TimeTravelControllerUnitTest {
    @InjectMocks
    private TimeTravelController timeTravelController;

    private MockMvc mockMvc;

    @Mock
    private TimeTravelService timeTravelService;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String URL_PATH = "/v1/travels";

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(timeTravelController)
                .setControllerAdvice(ExceptionController.class)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void should_return_ok_when_find_existing_travel_by_id() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A12345")
                .date(LocalDate.of(2020,10,11))
                .place("London").build();

        when(timeTravelService.getTravel(new ObjectId("507f191e810c19729de860ea"))).thenReturn(newTravel);

        mockMvc.perform(get(URL_PATH + "/507f191e810c19729de860ea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("place", is("London")))
                .andExpect(jsonPath("pgi", is("A12345")));
    }

    @Test
    void should_return_not_found_when_travel_id_dont_exist() throws Exception {
        when(timeTravelService.getTravel(new ObjectId("317f191e810c19729de860fa"))).thenThrow(new TravelNotFoundException(""));
        mockMvc.perform(get(URL_PATH + "/317f191e810c19729de860fa")).andExpect(status().isNotFound());
    }

    @Test
    void should_return_created_with_location_when_creating_new_travel() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A1234567")
                .date(LocalDate.of(2020,10,10))
                .place("Brazil").build();

        when(timeTravelService.saveTravel(newTravel)).thenReturn("507f191e810c19729de860ea");

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(header().stringValues("location", "http://localhost/v1/travels/507f191e810c19729de860ea"));
    }

    @Test
    void should_return_internal_server_error_when_has_an_exception() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A1234567")
                .date(LocalDate.of(2020,10,10))
                .place("Brazil").build();

        when(timeTravelService.saveTravel(newTravel)).thenThrow(new RuntimeException());

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_return_conflict_when_there_is_a_paradox_exception() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A12345")
                .date(LocalDate.of(2020,10,11))
                .place("London").build();

        when(timeTravelService.saveTravel(newTravel)).thenThrow(new ParadoxException("Paradox detected! traveler with pgi A12345 already traveled to London at date 2020-10-11"));

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.description", is("Paradox detected! traveler with pgi A12345 already traveled to London at date 2020-10-11")));
    }

    @Test
    void should_return_no_content_when_deleting_an_travel() throws Exception {
        mockMvc.perform(delete(URL_PATH + "/507f191e810c19729de860aa")).andExpect(status().isNoContent());
    }

    @Test
    void should_return_ok_with_all_travels_when_get_travel_with_paging_filter() throws Exception {

        var travel1 = TimeTravelDTO.builder()
                .pgi("A1234")
                .date(LocalDate.of(2020, 10, 10)).place("London")
                .build();

        var travel2 = TimeTravelDTO.builder()
                .pgi("B1234")
                .date(LocalDate.of(2020, 10, 11))
                .place("Brazil").build();

        when(timeTravelService.getTravels(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(travel1, travel2)));

        mockMvc.perform(get(URL_PATH).param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pgi", is("A1234")))
                .andExpect(jsonPath("$.content[0].place", is("London")));
    }
}
