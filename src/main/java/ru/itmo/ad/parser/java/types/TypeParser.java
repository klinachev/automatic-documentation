package ru.itmo.ad.parser.java.types;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;

public class TypeParser {
    private int i = 0;

    public TypeRef tryParse(Scanner sc) {
        i = 0;
        sc.dropWhitespaces();
        var res = tryParseType(sc);
        if (res == null) {
            return null;
        }
        sc.dropUntil(i);
        return res;
    }

    private TypeRef tryParseType(Scanner sc) {
        int start = i;
        skipWhitespaces(sc);
        sc.load(i + 1);
        if (!Character.isAlphabetic(sc.charAt(i))) {
            return null;
        }
        while (isTypePart(sc, i)) {
            sc.load(++i + 1);
        }
        int end = i;
        var list = new ArrayList<TypeRef>();
        if (sc.charAt(i) == '<') {
            i++;
            skipWhitespaces(sc);
            if (sc.charAt(i) != '>') {
                var ref = tryParseType(sc);
                if (ref != null) {
                    list.add(ref);
                }
                skipWhitespaces(sc);
                while (sc.charAt(i) != '>') {
                    if (sc.charAt(i++) != ',') {
                        throw new ParseException();
                    }
                    ref = tryParseType(sc);
                    list.add(ref);
                    skipWhitespaces(sc);
                }
            }
            i++;
        }
        boolean isArray = sc.charAt(i) == '[' && sc.charAt(i + 1) == ']';
        if (isArray) {
            i += 2;
        }
        return new TypeRef(sc.getString().substring(start, end), list, isArray);
    }

    private void skipWhitespaces(Scanner sc) {
        while (Character.isWhitespace(sc.charAt(i))) {
            sc.load(++i + 1);
        }
    }

    private static boolean isTypePart(Scanner sc, int i) {
        char ch = sc.charAt(i);
        return Character.isAlphabetic(ch) || Character.isDigit(ch)
                || ch == '.' || ch == '_' || ch == ':';
    }
}
