package ru.itmo.ad.parser.java.types;

import java.util.List;
import java.util.stream.Collectors;

public record TypeRef(String name, List<TypeRef> templates, boolean isArray) {

    @Override
    public String toString() {
        String result = name;
        if (!templates.isEmpty()) {
            result += templates.stream().map(TypeRef::toString).collect(Collectors.joining(", ", "<", ">"));
        }
        if (isArray) {
            result += "[]";
        }
        return result;
    }
}
