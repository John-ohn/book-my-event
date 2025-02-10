package com.BookMyEvent.annotations;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.dto.EventDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StartTimeBeforeEndTimeValidator implements ConstraintValidator<StartTimeBeforeEndTime, EventDTO> {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  @Override
  public boolean isValid(EventDTO eventDTO , ConstraintValidatorContext context) {
    if (eventDTO.getDate() == null || eventDTO.getDate().time() == null || eventDTO.getDate().endTime() == null) {
      return true;
    }
    try {
      LocalTime startTime = LocalTime.parse(eventDTO.getDate().time(), TIME_FORMATTER);
      LocalTime endTime = LocalTime.parse(eventDTO.getDate().endTime(), TIME_FORMATTER);
      boolean isValid = startTime.isBefore(endTime);

          if (!isValid) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode("startTime")
          .addConstraintViolation();
    }

      return isValid;
    } catch (Exception e) {
      return false;
    }
  }
}
