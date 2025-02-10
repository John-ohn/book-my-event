package com.BookMyEvent.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToLocalTimeConverter implements Converter<String, LocalTime> {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  @Override
  public LocalTime convert(String source) {
    return LocalTime.parse(source, FORMATTER);
  }
}

