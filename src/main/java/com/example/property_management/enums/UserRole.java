package com.example.property_management.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    GUEST(0),
    OWNER(1),
    AGENT(2),
    AGENT_ADMIN(3),
    MANAGER(4),
    ADMIN(5);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

}