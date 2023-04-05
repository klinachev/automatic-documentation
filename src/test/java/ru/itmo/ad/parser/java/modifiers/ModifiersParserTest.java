package ru.itmo.ad.parser.java.modifiers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.utils.Scanner;

class ModifiersParserTest {
    ModifiersParser modifiersParser = new ModifiersParser();

    @Test
    void testEmpty() {
        var target = "";
        var m = modifiersParser.parse(new Scanner(target));
        Assertions.assertNull(m);
    }

    @Test
    void testNoModifiers() {
        var target = "record A {}";
        var m = modifiersParser.parse(new Scanner(target));
        Assertions.assertEquals(
                new Modifiers(false, false, Modifiers.Privacy.DEFAULT, false, Modifiers.ClassType.RECORD, false),
                m
        );
    }

    @Test
    void testAllModifiers() {
        var target = "private final static Foo foo;";
        var m = modifiersParser.parse(new Scanner(target));
        Modifiers expected = new Modifiers(true, true, Modifiers.Privacy.PRIVATE, false, null, false);
        Assertions.assertEquals(expected, m);

        var target2 = "private static final Foo foo;";
        var m2 = modifiersParser.parse(new Scanner(target2));
        Assertions.assertEquals(expected, m2);
    }
}