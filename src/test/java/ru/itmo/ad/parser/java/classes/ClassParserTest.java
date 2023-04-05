package ru.itmo.ad.parser.java.classes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.utils.Scanner;

class ClassParserTest {
    ClassParser classParser = new ClassParser();

    @Test
    void testNoClasses() {
        var target = "";
        var classModel = classParser.parse(new Scanner(target));
        Assertions.assertNull(classModel);
    }

    @Test
    void testSimpleClass() {
        var target = """
                public class TestClass {
                    static void main() {
                            var argumentsResolver = new ArgumentsResolver();
                            var documentationApi = new DocumentationApi();
                            var arguments = argumentsResolver.resolve(args);
                    }
                }
                """;
        var classModel = classParser.parse(new Scanner(target));
        ClassElement.Class testClass = new ClassElement.Class("TestClass",
                new Modifiers(false, false, Modifiers.Privacy.PUBLIC, false, Modifiers.ClassType.CLASS, false));

        Assertions.assertEquals(testClass.name(), classModel.name());
        Assertions.assertEquals(testClass.modifiers(), classModel.modifiers());
    }
}