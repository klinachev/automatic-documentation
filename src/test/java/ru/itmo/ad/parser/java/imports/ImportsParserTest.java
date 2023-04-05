package ru.itmo.ad.parser.java.imports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.List;

class ImportsParserTest {
    ImportsParser importsParser = new ImportsParser();

    @Test
    void testNoImports() {
        var target = "class A {}";
        var imports = parse(target);
        Assertions.assertTrue(imports.isEmpty());
    }

    @Test
    void testSimpleImports() {
        var target = """
                import aa.b.c.D;
                import static a.b.Efg.test;
                import a.b.Efg;
                class A {}
                """;
        var imports = parse(target);
        Assertions.assertEquals(
                List.of(new Import(ImportType.DEFAULT, "aa.b.c.D"),
                        new Import(ImportType.STATIC, "a.b.Efg.test"),
                        new Import(ImportType.DEFAULT, "a.b.Efg")),
                imports);
    }

    @Test
    void testImportsWithSpaces() {
        var target = """
                   import aa .b.c  .D ;
                    import static a.  b.  Efg  . test  ;
                  import a.   b.    Efg ;
                class A {}
                """;
        var imports = parse(target);
        Assertions.assertEquals(
                List.of(new Import(ImportType.DEFAULT, "aa.b.c.D"),
                        new Import(ImportType.STATIC, "a.b.Efg.test"),
                        new Import(ImportType.DEFAULT, "a.b.Efg")),
                imports);
    }

    private List<Import> parse(String source) {
        return importsParser.parse(new Scanner(source));
    }
}