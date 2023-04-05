package ru.itmo.ad.parser.java.classes;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.modifiers.ModifiersParser;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.types.TypeParser;
import ru.itmo.ad.parser.java.utils.Scanner;

public class VariableParser {
    private final ModifiersParser modifiersParser = new ModifiersParser();

    private final ObjectParser objectParser = new ObjectParser();

    private final TypeParser typeParser = new TypeParser();

    public Variable parse(Scanner sc, Modifiers modifiers) {
        String type = typeParser.tryParse(sc);
        if (type == null) {
            return null;
        }
        DefaultObject name = objectParser.tryTakeName(sc);
        if (name == null) {
            throw new ParseException("Name expected");
        }
        return new Variable(type, name.value(), modifiers);
    }
}
