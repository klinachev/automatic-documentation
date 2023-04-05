package ru.itmo.ad.parser.java.imports;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

import static ru.itmo.ad.parser.java.JavaParser.substringWithoutSpaces;

public class ImportsParser {
    private static final String IMPORT_STRING = "import";

    private static final String STATIC_STRING = "static";

    public List<Import> parse(Scanner scanner) {
        var imports = new ArrayList<Import>();
        var lastImport = parseImport(scanner);
        while (lastImport != null) {
            imports.add(lastImport);
            lastImport = parseImport(scanner);
        }
        return imports;
    }

    private Import parseImport(Scanner scanner) {
        boolean found = scanner.takeString(IMPORT_STRING);
        if (!found) {
            return null;
        }
        boolean foundStatic = scanner.takeString(STATIC_STRING);
        var type = foundStatic ? ImportType.STATIC : ImportType.DEFAULT;
        int pos = scanner.loadUntil(';');
        if (pos == -1) {
            throw new ParseException();
        }
        var string = scanner.getString();
        scanner.dropUntil(pos + 1);
        var importString = substringWithoutSpaces(string, 0, pos);
        return new Import(type, importString);
    }
}