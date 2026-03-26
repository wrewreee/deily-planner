package com.example.dailyplanner.enums;

public enum TaskSortField {
    TITLE("title"),
    DEADLINE("deadline"),
    PRIORITY("priority"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String fieldName;

    TaskSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
