package ru.itmo.ad.parser.java.types;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.utils.Scanner;

public class TypeParser {
    private final ObjectParser objectParser = new ObjectParser();

    public String tryParse(Scanner sc) {
        sc.dropWhitespaces();
        int i = tryParse(sc, 0);
        if (i == -1) {
            return null;
        }
        var type = sc.getString().substring(0, i);
        sc.dropUntil(i);
        return type;
    }

    private int tryParse(Scanner sc, int i) {
        i = skipWhitespaces(sc, i);
        sc.load(i + 1);
        if (!Character.isAlphabetic(sc.charAt(i))) {
            return -1;
        }
        while (isTypePart(sc, i)) {
            sc.load(++i + 1);
        }
        if (sc.charAt(i) == '<') {
            i++;
            i = skipWhitespaces(sc, i);
            if (sc.charAt(i) != '>') {
                i = tryParse(sc, i + 1);
                i = skipWhitespaces(sc, i);
                while (sc.charAt(i) != '>') {
                    if (sc.charAt(i++) != ',') {
                        throw new ParseException();
                    }
                    i = tryParse(sc, i + 1);
                    i = skipWhitespaces(sc, i);
                }
            }
            i++;
        }
        if (sc.charAt(i) == '[') {
            int pos = objectParser.numberPos(sc, ++i);
            if (pos != -1) {
                i = pos;
            }
            if (sc.charAt(i) == ']') {
                i++;
            } else {
                throw new ParseException();
            }
        }
        return i;
    }

    private static int skipWhitespaces(Scanner sc, int i) {
        while (Character.isWhitespace(sc.charAt(i))) {
            sc.load(++i + 1);
        }
        return i;
    }

    private static boolean isTypePart(Scanner sc, int i) {
        char ch = sc.charAt(i);
        return Character.isAlphabetic(ch) || Character.isDigit(ch)
                || ch == '.' || ch == '_' || ch == ':';
    }
}
