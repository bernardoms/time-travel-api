package com.bernardoms.timetravelapi.integration;

import com.bernardoms.timetravelapi.model.TimeTravel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;

@SpringBootTest
public abstract class IntegrationTest {


    private static boolean alreadySaved = false;

    @Autowired
    MongoTemplate mongoTemplate;


    @BeforeEach
    public void setUp() {

        if (alreadySaved) {
            return;
        }
        mongoTemplate
                .save(TimeTravel
                                .builder()
                                .id(new ObjectId("507f191e810c19729de860ea"))
                                .pgi("A12345")
                                .place("London")
                                .date(LocalDate.of(2020, 10, 11)), "travels");
        mongoTemplate
                .save(TimeTravel
                        .builder()
                        .id(new ObjectId("507f191e810c19729de860eb"))
                        .pgi("A12346")
                        .place("London")
                        .date(LocalDate.of(2020, 10, 12)), "travels");
        alreadySaved = true;
    }
}