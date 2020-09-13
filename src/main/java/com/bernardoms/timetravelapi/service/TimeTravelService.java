package com.bernardoms.timetravelapi.service;

import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimeTravelService {
    String saveTravel(TimeTravelDTO timeTravelDTO) throws ParadoxException;
    TimeTravelDTO getTravel(ObjectId travelId) throws TravelNotFoundException;
    Page<TimeTravelDTO> getTravels(Pageable pageable);
    void deleteTravel(ObjectId travelId);
}
