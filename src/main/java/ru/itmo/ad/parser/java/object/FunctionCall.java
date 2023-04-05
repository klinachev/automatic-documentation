package ru.itmo.ad.parser.java.object;

import java.util.List;

public record FunctionCall(Object source, List<Object> args, boolean constructor) implements Object {
}
