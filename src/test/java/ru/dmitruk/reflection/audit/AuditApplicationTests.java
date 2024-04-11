package ru.dmitruk.reflection.audit;

import org.junit.jupiter.api.Test;
import ru.dmitruk.reflection.audit.entity.AuditEntity;
import ru.dmitruk.reflection.audit.entity.AuditField;
import ru.dmitruk.reflection.audit.service.FieldChangeCheckService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AuditApplicationTests {

    private final FieldChangeCheckService checkService = new FieldChangeCheckService();

    @Test
    void test_returnedResultWithDifferenceBetweenFields() {
        AuditEntity oldVersion = new AuditEntity("Nastya", "Dmitruk", 20);
        AuditEntity newVersion = new AuditEntity("Vika", "Smirnova", 15);


        var result = checkService.check(oldVersion, newVersion, AuditField.class, "displayName");


        assertAll(
                () -> assertThat("first_field", result.get(0), equalTo("Field: 'checkedName' was changed from: 'Nastya' to: 'Vika'")),
                () -> assertThat("second_field", result.get(1), equalTo("Field: 'checkedAge' was changed from: '20' to: '15'")),
                () -> assertThat("size", result.size(), equalTo(2))
        );
    }

    @Test
    void test_returnedEmptyResult() {
        AuditEntity oldVersion = new AuditEntity("Nastya", "Dmitruk", 20);
        AuditEntity newVersion = new AuditEntity("Nastya", "Dmitruk", 20);


        var result = checkService.check(oldVersion, newVersion, AuditField.class, "displayName");


        assertThat("empty_list", result.isEmpty(), equalTo(true));
    }

    @Test
    void test_throwException() {
        AuditEntity oldVersion = new AuditEntity("Nastya", "Dmitruk", 20);
        Object newVersion = new Object();


        assertThrows(IllegalArgumentException.class,
                () -> checkService.check(oldVersion, newVersion, AuditField.class, "displayName"),
                "Try to check different entity"
        );
    }
}
