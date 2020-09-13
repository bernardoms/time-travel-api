package com.bernardoms.timetravelapi.controller;

import com.bernardoms.timetravelapi.dto.TimeTravelDTO;
import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import com.bernardoms.timetravelapi.service.TimeTravelService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/v1/travels")
@RestController
@RequiredArgsConstructor
public class TimeTravelController {
    private final TimeTravelService timeTravelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> save(@RequestBody @Validated TimeTravelDTO timeTravelDTO, UriComponentsBuilder uriComponentsBuilder) throws ParadoxException {
        var travelId = timeTravelService.saveTravel(timeTravelDTO);

        var uriComponent = uriComponentsBuilder.path("/v1/travels/{travelId}").buildAndExpand(travelId);

        return ResponseEntity.created(uriComponent.toUri()).build();
    }

    @GetMapping("/{travelId}")
    @ResponseStatus(HttpStatus.OK)
    public TimeTravelDTO getTravel(@PathVariable ObjectId travelId) throws TravelNotFoundException {
        return timeTravelService.getTravel(travelId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TimeTravelDTO> getTravels(Pageable pageable) {
        return timeTravelService.getTravels(pageable);
    }
    
    @DeleteMapping("/{travelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTravel(@PathVariable ObjectId travelId) {
        timeTravelService.deleteTravel(travelId);
    }
}
