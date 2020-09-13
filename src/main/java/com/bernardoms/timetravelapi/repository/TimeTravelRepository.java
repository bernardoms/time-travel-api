package com.bernardoms.timetravelapi.repository;

import com.bernardoms.timetravelapi.model.TimeTravel;
import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TimeTravelRepository extends PagingAndSortingRepository<TimeTravel, ObjectId> {
    Optional<TimeTravel> findByPgiAndDate(String pgi, LocalDate date);
}
