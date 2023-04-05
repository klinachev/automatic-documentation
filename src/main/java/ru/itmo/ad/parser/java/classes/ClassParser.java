package ru.itmo.ad.parser.java.classes;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.annotation.Annotation;
import ru.itmo.ad.parser.java.annotation.AnnotationParser;
import ru.itmo.ad.parser.java.comment.CommentParser;
import ru.itmo.ad.parser.java.expression.ExpressionParser;
import ru.itmo.ad.parser.java.method.MethodParser;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.modifiers.ModifiersParser;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.types.TypeParser;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClassParser {

    private final MethodParser methodParser = new MethodParser();

    private final TypeParser typeParser = new TypeParser();

    private final ModifiersParser modifiersParser = new ModifiersParser();

    private final ObjectParser objectParser = new ObjectParser();
    private final ExpressionParser expressionParser = new ExpressionParser();

    private final CommentParser commentParser = new CommentParser();

    private final AnnotationParser annotationParser = new AnnotationParser();

    public ClassElement.Class parse(Scanner sc) {
        var commentsAndAnnotations = beforeBlock(sc);
        var modifiers = modifiersParser.parse(sc);
        if (modifiers == null || !modifiers.isClass()) {
            return null;
        }
        var classModel = tryParseClass(sc, modifiers);
        classModel.annotations().addAll(commentsAndAnnotations.annotations);
        classModel.comments().addAll(commentsAndAnnotations.comments);
        return classModel;
    }

    private void parseEnumValue(Scanner sc) {
        DefaultObject name = objectParser.tryTakeName(sc);
        if (sc.takeString("(")) {
            expressionParser.parseFunctionCall(sc);
        }
    }

    private ClassElement.Class tryParseClass(Scanner sc, Modifiers modifiers) {
        var classModel = parseName(sc, modifiers);
        if (modifiers.classType() == Modifiers.ClassType.ENUM) {
            parseEnumValue(sc);
            while (sc.takeString(",")) {
                if (sc.takeString(";")) {
                    break;
                }
                parseEnumValue(sc);
            }
            sc.takeString(";");
        }
        while (!sc.takeString("}")) {
            var classElement = parseBlock(sc);
            switch (classElement) {
                case ClassElement.Field f -> classModel.fields().add(f);
                case ClassElement.Class aClass -> classModel.classes().add(aClass);
                case ClassElement.Method method -> classModel.methods().add(method);
            }
        }
        return classModel;
    }

    private ClassElement.Class parseName(Scanner sc, Modifiers modifiers) {
        String name = typeParser.tryParse(sc);
        if (modifiers.classType() == Modifiers.ClassType.RECORD) {
            List<Variable> args = methodParser.parseArguments(sc);
        }
        if (sc.takeString("extends")) {
            String type = typeParser.tryParse(sc);
        }
        if (sc.takeString("implements")) {
            do {
                String type = typeParser.tryParse(sc);
            } while (sc.takeString(","));
        }
        if (sc.takeString("permits")) {
            do {
                String type = typeParser.tryParse(sc);
            } while (sc.takeString(","));
        }
        sc.takeStringOrThrow("{");
        return new ClassElement.Class(name, modifiers);
    }

    private CommentsAndAnnotations beforeBlock(Scanner sc) {
        var caa = new CommentsAndAnnotations(new ArrayList<>(), new ArrayList<>());
        List<String> comment = commentParser.parse(sc);
        Annotation annotation = annotationParser.parse(sc);
        caa.comments.addAll(comment);
        if (annotation != null) {
            caa.annotations.add(annotation);
        }
        while (!comment.isEmpty() || annotation != null) {
            comment = commentParser.parse(sc);
            annotation = annotationParser.parse(sc);
            caa.comments.addAll(comment);
            if (annotation != null) {
                caa.annotations.add(annotation);
            }
        }
        return caa;
    }

    private record CommentsAndAnnotations(List<String> comments, List<Annotation> annotations) {
    }

    private ClassElement parseBlock(Scanner sc) {
        var commentsAndAnnotations = beforeBlock(sc);
        Modifiers modifiers = modifiersParser.parse(sc);
        if (modifiers.isClass()) {
            var classModel = tryParseClass(sc, modifiers);
            addCommentsAndAnnotations(classModel, commentsAndAnnotations);
            return classModel;
        }
        int pos = sc.loadUntil(Set.of('=', '(', ';'));
        switch (sc.charAt(pos)) {
            case '(' -> {
                var method = methodParser.parse(sc, modifiers);
                addCommentsAndAnnotations(method, commentsAndAnnotations);
                return method;
            }
            case ';' -> {
                var type = typeParser.tryParse(sc);
                var name = objectParser.tryTakeName(sc);
                sc.takeStringOrThrow(";");
                ClassElement.Field field = new ClassElement.Field(new Variable(type, name.value(), modifiers));
                addCommentsAndAnnotations(field, commentsAndAnnotations);
                return field;
            }
            case '=' -> {
                var type = typeParser.tryParse(sc);
                DefaultObject name = objectParser.tryTakeName(sc);
                sc.takeStringOrThrow("=");
                expressionParser.parseStatement(sc);
                sc.takeStringOrThrow(";");
                ClassElement.Field field = new ClassElement.Field(new Variable(type, name.value(), modifiers));
                addCommentsAndAnnotations(field, commentsAndAnnotations);
                return field;
            }
            default -> throw new ParseException();
        }
    }

    private static void addCommentsAndAnnotations(
            ClassElement classElement,
            CommentsAndAnnotations commentsAndAnnotations
    ) {
        classElement.annotations().addAll(commentsAndAnnotations.annotations);
        classElement.comments().addAll(commentsAndAnnotations.comments);
    }
}
