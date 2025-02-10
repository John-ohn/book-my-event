package com.BookMyEvent.entity;

import jakarta.validation.constraints.NotBlank;

public record Location(String city, String street, String venue, String latitude, String longitude) {
}
