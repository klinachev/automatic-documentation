package ru.itmo.ad.parser.java.annotation;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.Object;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.HashMap;
import java.util.Map;

import static ru.itmo.ad.parser.java.annotation.Annotation.DEFAULT_ARG;

public class AnnotationParser {

    private final ObjectParser objectParser = new ObjectParser();

    public Annotation parse(Scanner scanner) {
        boolean found = scanner.takeString("@");
        if (!found) {
            return null;
        }
        int spaceOrChar = scanner.loadUntilSpaceOrChar('(');
        if (spaceOrChar == -1) {
            throw new ParseException("Unexpected end");
        }
        var name = scanner.getString().substring(0, spaceOrChar);
        scanner.dropUntil(spaceOrChar);
        var hasArgs = scanner.takeString("(");
        if (!hasArgs) {
            return new Annotation(name, Map.of());
        }
        Object object = objectParser.tryTakeObject(scanner);
        if (object instanceof DefaultObject v) {
            boolean isKey = scanner.takeString("=");
            if (!isKey) {
                return new Annotation(name, Map.of(DEFAULT_ARG, object));
            }
            var map = new HashMap<String, Object>();
            Object value = objectParser.tryTakeObject(scanner);
            map.put(v.value(), value);
            while (!scanner.takeString(")")) {
                scanner.takeStringOrThrow(",");
                var key = objectParser.tryTakeName(scanner);
                if (key == null) {
                    throw new ParseException("Argument name expected");
                }
                scanner.takeStringOrThrow("=");
                Object value1 = objectParser.tryTakeObject(scanner);
                if (value1 == null) {
                    throw new ParseException("Argument name expected");
                }
                map.put(key.value(), value1);
            }
            return new Annotation(name, map);
        } else {
            scanner.takeStringOrThrow(")");
            return new Annotation(name, Map.of(DEFAULT_ARG, object));
        }
    }
}
