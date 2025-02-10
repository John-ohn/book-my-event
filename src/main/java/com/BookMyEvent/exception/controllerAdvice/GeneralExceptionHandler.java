package com.BookMyEvent.exception.controllerAdvice;

import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GeneralExceptionHandler {

  @Value("${spring.servlet.multipart.max-file-size}")
  private String maxFileSize;

  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(GeneralException exception) {
    ErrorResponseDto errorResponse = new ErrorResponseDto(exception.getHttpStatus().value(), exception.getMessage());
    log.info("From GeneralExceptionHandler::handleGeneralException. Send message error ({})", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeException(MaxUploadSizeExceededException exception) {
    String message = String.format(" Maximum upload size is %s.", maxFileSize);
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.PAYLOAD_TOO_LARGE.value(), exception.getMessage()+message);
    log.info("From GeneralExceptionHandler::handleMaxUploadSizeException. Send message error ({})", exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new HashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), exception.getBody().getDetail(),errors);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
