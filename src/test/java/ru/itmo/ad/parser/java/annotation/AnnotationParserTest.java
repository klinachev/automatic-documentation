package ru.itmo.ad.parser.java.annotation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.StringObject;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.Map;

class AnnotationParserTest {
    AnnotationParser annotationParser = new AnnotationParser();

    @Test
    void testNoAnnotation() {
        var target = "class A {}";
        var annotation = parse(target);
        Assertions.assertNull(annotation);
    }

    @Test
    void testSimpleAnnotation() {
        var target = """
                @Test
                @Test2
                class A {}
                """;
        var annotation = parse(target);
        Assertions.assertEquals(new Annotation("Test", Map.of()), annotation);
    }


    @Test
    void testAnnotationWithArgs() {
        var target = """
                @Deprecated(forRemoval = true, since = "1.2.3")
                @Test2
                class A {}
                """;
        var annotation = parse(target);
        Assertions.assertEquals(
                new Annotation("Deprecated",
                        Map.of("forRemoval", new DefaultObject("true"),
                                "since", new StringObject("\"1.2.3\"")))
                , annotation);
    }

    @Test
    void testAnnotationWithSpaces() {
        var target = """
                @  Deprecated  (  forRemoval   = true  , since = " \\n 1.2  .3 ")
                @Test2
                class A {}
                """;
        var annotation = parse(target);
        Assertions.assertEquals(
                new Annotation("Deprecated",
                        Map.of("forRemoval", new DefaultObject("true"),
                                "since", new StringObject("\" n 1.2  .3 \"")))
                , annotation);
    }

    private Annotation parse(String source) {
        return annotationParser.parse(new Scanner(source));
    }

}