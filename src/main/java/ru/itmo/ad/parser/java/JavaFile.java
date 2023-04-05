package ru.itmo.ad.parser.java;

import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.imports.Import;

import java.util.List;
import java.util.Map;

public record JavaFile(
        String name,
        String packageName,
        Map<String, Import> imports,
        List<ClassElement.Class> classes
) {

    public String fullName() {
        return packageName + "." + name;
    }
}
