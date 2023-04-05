package ru.itmo.ad.parser.java.object;

public record Operator(Object left, Object right, String operator) implements Object {
}
