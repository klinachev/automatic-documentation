package ru.itmo.ad.parser.java.method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.classes.Variable;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.List;

class MethodParserTest {

    MethodParser methodParser = new MethodParser();

    @Test
    void testNoArguments() {
        var target = "()";
        var args = methodParser.parseArguments(new Scanner(target));
        var target2 = "( \n )";
        var args2 = methodParser.parseArguments(new Scanner(target2));
        Assertions.assertTrue(args.isEmpty());
        Assertions.assertTrue(args2.isEmpty());
    }

    @Test
    void testArguments() {
        var target = "(String name, Boolean check  , Modifier modif )";
        var args = methodParser.parseArguments(new Scanner(target));
        Assertions.assertEquals(
                List.of(
                        new Variable("String", "name", null),
                        new Variable("Boolean", "check", null),
                        new Variable("Modifier", "modif", null)
                ),
                args
        );
    }

    @Test
    void testNoThrows() {
        var target = " { ... }";
        var args = methodParser.parseThrows(new Scanner(target));
        Assertions.assertTrue(args.isEmpty());
    }

    @Test
    void testThrows() {
        var target = " throws RuntimeException  , ParseException { ";
        var args = methodParser.parseThrows(new Scanner(target));
        Assertions.assertEquals(
                List.of("RuntimeException", "ParseException"),
                args
        );
    }

    @Test
    @Disabled
    void testParseBody() {
        var target = """
                                var a = 1 + 3;
                                int b = 2;
                                }
                """;
        var args = methodParser.parseBody(new Scanner(target));
        Assertions.assertEquals(
                List.of("RuntimeException", "ParseException"),
                args
        );
    }
}