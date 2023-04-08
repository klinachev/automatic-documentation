package ru.itmo.ad.parser.java;

import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.classes.ClassParser;
import ru.itmo.ad.parser.java.imports.ImportsParser;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class JavaParser {

    private final PackageParser packageParser = new PackageParser();
    private final ImportsParser importsParser = new ImportsParser();

    private final ClassParser classParser = new ClassParser();

    public JavaFile parse(Scanner scanner, String name) {
        try {
            System.out.println("Starting " + name);
            var mainClassName = name.substring(0, name.length() - 5); // removes ".java"
            var packageName = packageParser.parse(scanner);
            var imports = importsParser.parse(scanner);
            var list = new ArrayList<ClassElement.Class>();
            var clazz = classParser.parse(scanner);
            while (clazz != null) {
                list.add(clazz);
                clazz = classParser.parse(scanner);
            }
            System.out.println("Finished " + name);
            return new JavaFile(name,
                    packageName,
                    imports.stream()
                            .filter(imp -> !imp.name().endsWith(".*"))
                            .collect(Collectors.toMap(
                                    imp -> imp.name().substring(imp.name().lastIndexOf('.') + 1),
                                    it -> it
                            )),
                    list.stream()
                            .filter(classElement -> classElement.type().name().equals(mainClassName))
                            .findFirst().orElseThrow(),
                    list
            );
        } catch (Throwable p) {
            System.err.println(scanner.getString());
            throw p;
        }
    }

    public static String substringWithoutSpaces(String string, int start, int end) {
        var sb = new StringBuilder();
        for (int i = start; i < end; ++i) {
            char ch = string.charAt(i);
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
