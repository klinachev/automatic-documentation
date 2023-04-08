package ru.itmo.ad.parser.java.classes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.types.TypeRef;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;

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
        ClassElement.Class testClass = new ClassElement.Class(new TypeRef("TestClass", new ArrayList<>(), false),
                new Modifiers(false, false, Modifiers.Privacy.PUBLIC, false, Modifiers.ClassType.CLASS, false), null);

        Assertions.assertEquals(testClass.type(), classModel.type());
        Assertions.assertEquals(testClass.modifiers(), classModel.modifiers());
    }
}