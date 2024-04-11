package ru.dmitruk.reflection.audit.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuditEntity {

    @AuditField(displayName = "checkedName")
    private String name;
    private String surname;
    @AuditField(displayName = "checkedAge")
    private int age;
}
