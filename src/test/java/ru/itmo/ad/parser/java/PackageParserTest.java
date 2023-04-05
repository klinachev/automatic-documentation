package ru.itmo.ad.parser.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.utils.Scanner;


class PackageParserTest {
    PackageParser packageParser = new PackageParser();

    @Test
    void testNoPackage() {
        var target = "import a.b.c;";
        String pack = parse(target);
        Assertions.assertEquals("", pack);
    }

    @Test
    void testSimplePackage() {
        var target = """
        package a.b.c;
        """;
        String pack = parse(target);
        Assertions.assertEquals("a.b.c", pack);
    }

    @Test
    void testPackageWithSpaces() {
        var target = """
           package a\s
            .b.   c  ;
        """;
        String pack = parse(target);
        Assertions.assertEquals("a.b.c", pack);
    }

    private String parse(String source) {
        return packageParser.parse(new Scanner(source));
    }
}