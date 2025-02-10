package com.BookMyEvent.annotations;

import com.BookMyEvent.entity.dto.EventDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NotPastDateValidator implements ConstraintValidator<NotPastDate, EventDTO> {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  public boolean isValid(EventDTO eventDTO, ConstraintValidatorContext context) {
    if (eventDTO.getDate().day() == null || eventDTO.getDate().day().isBlank()) {
      return true;
    }

    try {
      LocalDate inputDate = LocalDate.parse(eventDTO.getDate().day(), DATE_FORMATTER);
      LocalDate today = LocalDate.now();
      boolean isValid = !inputDate.isBefore(today);

      if (!isValid) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("day")
            .addConstraintViolation();
      }
      return isValid;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
