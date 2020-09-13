package com.bernardoms.timetravelapi.integration.controller;

import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.integration.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TimeTravelControllerIntegrationTest extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String URL_PATH = "/v1/travels";

    @Test
    void should_return_ok_when_find_existing_travel_by_id() throws Exception {
        mockMvc.perform(get(URL_PATH + "/507f191e810c19729de860ea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("place", is("London")))
                .andExpect(jsonPath("date", is("2020-10-11")))
                .andExpect(jsonPath("pgi", is("A12345")));
    }


    @Test
    void should_return_not_found_when_travel_id_dont_exist() throws Exception {
        mockMvc.perform(get(URL_PATH + "/317f191e810c19729de860fa")).andExpect(status().isNotFound());
    }

    @Test
    void should_return_created_with_location_when_creating_new_travel() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A1234567")
                .date(LocalDate.of(2020,10,10))
                .place("Brazil").build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(header().exists("location"));
    }

    @Test
    void should_return_bad_request_when_creating_new_travel_with_invalid_body() throws Exception {
        var newTravel = TimeTravelDTO.builder().build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description.date", is("must not be null")))
                .andExpect(jsonPath("$.description.pgi", is("must not be blank")))
                .andExpect(jsonPath("$.description.place", is("must not be blank")));
    }

    @Test
    void should_return_bad_request_when_creating_new_travel_with_invalid_pgi() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("153445").build();

        mockMvc.perform(post(URL_PATH).content(mapper.writeValueAsString(newTravel)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description.pgi", is("pgi should start with letter and be alphanumeric")));
    }

    @Test
    void should_return_conflict_when_creating_new_travel_when_a_travel_already_exist_with_same_date_pgi_and_location() throws Exception {
        var newTravel = TimeTravelDTO.builder().pgi("A12345")
                .date(LocalDate.of(2020,10,11))
                .place("London").build();


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
        mockMvc.perform(get(URL_PATH).param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pgi", is("A12345")))
                .andExpect(jsonPath("$.content[0].place", is("London")))
                .andExpect(jsonPath("$.content[0].date", is("2020-10-11")))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageable.offset", is(0)))
                .andExpect(jsonPath("$.last", is(false)));
    }
}
