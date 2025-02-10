package com.BookMyEvent;

import com.BookMyEvent.converter.LocalTimeToStringConverter;
import com.BookMyEvent.converter.StringToLocalTimeConverter;
import com.BookMyEvent.mapper.EventMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class TestConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public LocalTimeToStringConverter localTimeToStringConverter(){
    return  new LocalTimeToStringConverter();
  }

  @Bean
  public StringToLocalTimeConverter stringToLocalTimeConverter(){
    return  new StringToLocalTimeConverter();
  }

  @Bean
  public EventMapperImpl eventMapper() {
    return new EventMapperImpl();
  }

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("localhost");
    mailSender.setPort(3025);
//    mailSender.setUsername("test");
//    mailSender.setPassword("test");
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", "false");
    props.put("mail.smtp.starttls.enable", "false");
    return mailSender;
  }


}
