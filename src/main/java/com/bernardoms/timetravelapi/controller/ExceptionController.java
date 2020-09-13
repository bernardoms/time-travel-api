package com.bernardoms.timetravelapi.controller;

import com.bernardoms.timetravelapi.exception.ParadoxException;
import com.bernardoms.timetravelapi.exception.TravelNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private static final String DESCRIPTION = "description";

    @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private Object handleIllegalArgumentException(Exception ex, HttpServletRequest request) {
        log.error("invalid arguments/body for processing the request: " + request.getRequestURI(), ex);
        if (ex instanceof MethodArgumentTypeMismatchException
                && ((MethodArgumentTypeMismatchException) ex).getMostSpecificCause().getMessage().contains("invalid hexadecimal representation of an ObjectId")) {
            return Map.of(DESCRIPTION, "invalid " + ((MethodArgumentTypeMismatchException) ex).getName());
        }
        return mountError(ex);
    }

    @ExceptionHandler({ParadoxException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    private Object handleParadoxException(ParadoxException ex, HttpServletRequest request) {
        log.info("paradox exception : {}", ex.getMessage());
        return mountError(ex);
    }

    @ExceptionHandler({TravelNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private Object handleTravelNotFoundException(TravelNotFoundException ex, HttpServletRequest request) {
        log.info("travel not found! : " + request.getRequestURI(), ex);
        return mountError(ex);
    }


    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private Object handleException(Exception ex, HttpServletRequest request) {
        log.error("error on process the request: " + request.getRequestURI(), ex);
        return mountError(ex);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected Object handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(field -> details.put(field.getField(), field.getDefaultMessage()));

        log.info("error on the request validation {}", details);

        return Map.of(DESCRIPTION, details);
    }

    private HashMap<Object, Object> mountError(Exception e) {
        var error = new HashMap<>();
        error.put(DESCRIPTION, e.getMessage());
        return error;
    }
}
