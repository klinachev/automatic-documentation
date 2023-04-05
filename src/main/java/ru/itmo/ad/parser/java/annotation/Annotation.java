package ru.itmo.ad.parser.java.annotation;

import ru.itmo.ad.parser.java.object.Object;

import java.util.Map;

public record Annotation(String name, Map<String, Object> args) {
    public static final String DEFAULT_ARG = "default arg";
}
