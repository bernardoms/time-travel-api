package com.bernardoms.timetravelapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "travels")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTravel {
    @Id
    private ObjectId id;
    private String pgi;
    private String place;
    private LocalDate date;
}
