package ru.itmo.ad.calculation;

import ru.itmo.ad.FileWithPath;
import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.imports.Import;
import ru.itmo.ad.parser.java.imports.ImportType;

import java.nio.file.Path;
import java.util.Map;

public class ImportResolver {

    private final Map<String, FileWithPath> javaFiles;

    public ImportResolver(Map<String, FileWithPath> javaFiles) {
        this.javaFiles = javaFiles;
    }

    public FileWithPath resolveFile(FileWithPath source, String value) {
        int pos = value.indexOf('.');
        if (pos != -1) {
            value = value.substring(0, pos);
        }
        Import imp = source.javaFile().imports().get(value);
        if (imp == null) {
            if (value.equals(source.javaFile().classes().get(0).type().name())) {
                return source;
            }
            return javaFiles.get(source.javaFile().packageName() + "." + value + ".java");
        }
        if (imp.importType() == ImportType.DEFAULT) {
            return javaFiles.get(imp.name() + ".java");
        }
        return null;
    }

    public ClassElement.Class resolveClass(FileWithPath source, String value) {
        var file = resolveFile(source, value);
        if (file == null) {
            return null;
        }
        var main = file.javaFile().mainClass();
        if (main.type().name().equals(value)) {
            return main;
        }
        return file.javaFile().classes().stream()
                .filter(classElement -> classElement.type().name().equals(value))
                .findFirst().orElseThrow();
    }

    public Path resolve(FileWithPath source, String value) {
        var file = resolveFile(source, value);
        if (file == null) {
            return null;
        }
        return file.destination();
    }
}
