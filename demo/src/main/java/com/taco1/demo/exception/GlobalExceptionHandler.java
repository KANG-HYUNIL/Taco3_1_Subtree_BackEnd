package com.taco1.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    //여기에 작성되지 않은 모든 Exception을 처리하는 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e, WebRequest request)
    {

        return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
