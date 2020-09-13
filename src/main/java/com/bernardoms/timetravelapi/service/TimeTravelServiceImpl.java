package com.bernardoms.timetravelapi.service;

import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import com.bernardoms.timetravelapi.model.TimeTravel;
import com.bernardoms.timetravelapi.repository.TimeTravelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TimeTravelServiceImpl implements TimeTravelService {
    private final TimeTravelRepository timeTravelRepository;
    private final ModelMapper modelMapper;

    public String saveTravel(TimeTravelDTO timeTravelDTO) throws ParadoxException {
        Optional<TimeTravel> travelOptional = timeTravelRepository
                .findByPgiAndDate(timeTravelDTO.getPgi(), timeTravelDTO.getDate());

        if (travelOptional.isPresent()) {
            throw new ParadoxException("Paradox detected! traveler with pgi " + timeTravelDTO.getPgi() + " already traveled to " + timeTravelDTO.getPlace() + " at date " +
                    timeTravelDTO.getDate());
        }

        return timeTravelRepository
                .save(modelMapper.map(timeTravelDTO, TimeTravel.class))
                .getId()
                .toString();
    }

    @Cacheable(cacheNames = "travel")
    public TimeTravelDTO getTravel(ObjectId travelId) throws TravelNotFoundException {
        var timeTravel = timeTravelRepository
                .findById(travelId)
                .orElseThrow(() -> new TravelNotFoundException("Travel with id " + travelId + " not found!"));
        return modelMapper.map(timeTravel, TimeTravelDTO.class);
    }

    public Page<TimeTravelDTO> getTravels(Pageable pageable) {
        return timeTravelRepository
                .findAll(pageable)
                .map(t -> TimeTravelDTO.builder().pgi(t.getPgi()).place(t.getPlace()).date(t.getDate()).build());
    }

    @CacheEvict(cacheNames = "travel")
    public void deleteTravel(ObjectId travelId) {
        timeTravelRepository
                .deleteById(travelId);
        log.info("travel with id " + travelId + " deleted!");
    }
}
