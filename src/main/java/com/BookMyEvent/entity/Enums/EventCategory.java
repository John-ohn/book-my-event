package com.BookMyEvent.entity.Enums;

public enum EventCategory {
  TOP_EVENTS("Топ події"),
  POPULAR("Популярні"),
  RECOMMENDED("Рекомендовані");

  private final String ukrainianName;

  EventCategory(String ukrainianName) {
    this.ukrainianName = ukrainianName;
  }

  public String getUkrainianName() {
    return ukrainianName;
  }
}
