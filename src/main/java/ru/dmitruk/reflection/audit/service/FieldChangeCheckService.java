package ru.dmitruk.reflection.audit.service;

import javafx.util.Pair;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldChangeCheckService {

    public <T> List<String> check(
            T oldVersion, T newVersion, Class<? extends Annotation> annotation, String annotationMethodName
    ) {
        if (!oldVersion.getClass().equals(newVersion.getClass())) {
            throw new IllegalArgumentException("Try to check different entity");
        }
        var mapOfDifferentFieldsValues = getMapOfDifferentFieldsWithAnnotation(
                oldVersion, newVersion, annotation, annotationMethodName
        );
        return createMessagesWithDifferentFields(mapOfDifferentFieldsValues);
    }

    private <T> Map<String, Pair<Object, Object>> getMapOfDifferentFieldsWithAnnotation(
            T oldClass, T newClass,
            Class<? extends Annotation> annotation,
            String annotationMethodName
    ) {
        Map<String, Pair<Object, Object>> differentFieldValues = new HashMap<>();

        Arrays.stream(oldClass.getClass().getDeclaredFields())
                .forEach(field -> checkFieldWithAnnotationIntoMap(differentFieldValues, annotation,
                        annotationMethodName, field, oldClass, newClass)
                );

        return differentFieldValues;
    }

    @SneakyThrows
    private <T> void checkFieldWithAnnotationIntoMap(
            Map<String, Pair<Object, Object>> differentFieldValues,
            Class<? extends Annotation> annotation,
            String annotationMethodName,
            Field field,
            T oldClass, T newClass
    ) {
        if (field.isAnnotationPresent(annotation)) {
            field.setAccessible(true);
            Object oldFieldValue = field.get(oldClass);
            Object newFieldValue = field.get(newClass);

            var neededAnnotation = field.getAnnotation(annotation);

            compareFieldsValues(
                    getAnnotationMethodName(neededAnnotation, annotationMethodName),
                    differentFieldValues,
                    oldFieldValue, newFieldValue);
        }
    }


    private String getAnnotationMethodName(Annotation annotation, String annotationMethodName) {
        return (String) Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> annotationMethodName.equals(method.getName()))
                .map(method -> {
                    try {
                        return method.invoke(annotation, (Object[]) null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Annotation doesn't have method"));
    }

    private void compareFieldsValues(
            String annotatedFieldName,
            Map<String, Pair<Object, Object>> differentFieldValues,
            Object oldFieldValue, Object newFieldValue
    ) {
        if (!Objects.equals(oldFieldValue, newFieldValue)) {
            differentFieldValues.put(
                    annotatedFieldName,
                    new Pair<>(oldFieldValue, newFieldValue));
        }
    }

    private List<String> createMessagesWithDifferentFields(
            Map<String, Pair<Object, Object>> differentFieldValues
    ) {
        return differentFieldValues.entrySet().stream()
                .map(entry -> "Field: '" + entry.getKey()
                        + "' was changed from: '" + entry.getValue().getKey()
                        + "' to: '" + entry.getValue().getValue() + "'")
                .collect(Collectors.toList());
    }
}
