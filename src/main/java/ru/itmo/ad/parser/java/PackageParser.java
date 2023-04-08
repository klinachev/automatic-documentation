package ru.itmo.ad.parser.java;

import ru.itmo.ad.parser.java.utils.Scanner;

import static ru.itmo.ad.parser.java.JavaParser.substringWithoutSpaces;

public class PackageParser {
    private static final String PACKAGE_STRING = "package ";

    public String parse(Scanner scanner) {
        boolean found = scanner.takeString(PACKAGE_STRING);
        if (!found) {
            return "";
        }
        int pos = scanner.loadUntil(';');
        if (pos == -1) { // if eof reached
            throw new ParseException();
        }
        String string = scanner.getString();
        scanner.dropUntil(pos + 1);
        return substringWithoutSpaces(string, 0, pos);
    }

}
