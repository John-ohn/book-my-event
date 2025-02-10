package com.BookMyEvent.config;

import com.BookMyEvent.converter.LocalTimeToStringConverter;
import com.BookMyEvent.converter.StringToLocalTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class AppConfig {

  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    return new MongoCustomConversions(Arrays.asList(new StringToLocalTimeConverter(), new LocalTimeToStringConverter()));
  }

  @Bean
  protected PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("TaskScheduler-");
    scheduler.initialize();
    return scheduler;
  }
}