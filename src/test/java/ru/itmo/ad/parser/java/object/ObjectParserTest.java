package ru.itmo.ad.parser.java.object;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.utils.Scanner;

class ObjectParserTest {
    ObjectParser objectParser = new ObjectParser();

    @Test
    void testTryTakeJavaString() {
        var empty = objectParser.tryTakeJavaString(getScanner("   var a = \" \""));
        Assertions.assertNull(empty);
        var quotes = objectParser.tryTakeJavaString(getScanner("\"\\\"\""));
        Assertions.assertEquals("\"\"\"", quotes.value());
        var qoutes2 = objectParser.tryTakeJavaString(getScanner("\"'\""));
        Assertions.assertEquals("\"'\"", qoutes2.value());

        var target = " \"  \\\"  1 2 1\\23 \\\" \" ";
        var annotation = objectParser.tryTakeJavaString(getScanner(target));
        Assertions.assertEquals("\"  \"  1 2 123 \" \"", annotation.value());
    }

    @Test
    void testTryTakeNumber() {
        var empty = objectParser.tryTakeNumber(getScanner("   var a = 1"));
        Assertions.assertNull(empty);
        var simple = objectParser.tryTakeNumber(getScanner(" 1_1 2"));
        Assertions.assertEquals("1_1", simple.value());
        var longVal = objectParser.tryTakeNumber(getScanner(" 1L 2"));
        Assertions.assertEquals("1L", longVal.value());
        var doubleVal = objectParser.tryTakeNumber(getScanner(" 111.222d 2"));
        Assertions.assertEquals("111.222d", doubleVal.value());
    }

    private static Scanner getScanner(String source) {
        return new Scanner(source);
    }
}