package com.BookMyEvent.entity.Enums;

import java.util.Arrays;
import java.util.List;

public enum Role {

        VISITOR,
        ORGANIZER,
        ADMIN;

        public static List<Role> getAllRoles() {
                return Arrays.asList(Role.values());
        }
}
