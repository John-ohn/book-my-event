package com.BookMyEvent.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalTimeToStringConverter implements Converter<LocalTime, String> {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  @Override
  public String convert(LocalTime source) {
    return source.format(FORMATTER);
  }
}
