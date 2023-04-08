package ru.itmo.ad.parser.java.method;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.classes.VariableParser;
import ru.itmo.ad.parser.java.expression.Expression;
import ru.itmo.ad.parser.java.expression.ExpressionParser;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.types.TypeParser;
import ru.itmo.ad.parser.java.types.TypeRef;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

public class MethodParser {
    private final ExpressionParser expressionParser = new ExpressionParser();
    private final ObjectParser objectParser = new ObjectParser();
    private final TypeParser typeParser = new TypeParser();

    private final VariableParser variableParser = new VariableParser(typeParser);

    public ClassElement.Method parse(Scanner sc, Modifiers modifiers) {
        var type = typeParser.tryParse(sc);
        DefaultObject name = objectParser.tryTakeName(sc);
        if (type == null) {
            throw new ParseException("method expected");
        }
        if (name == null) { // constructor
            name = new DefaultObject(type.name());
        }
        List<Expression.Variable> arguments = parseArguments(sc);
        var throwables = parseThrows(sc);
        if (sc.takeString(";")) {
            return new ClassElement.Method(type, name.value(), modifiers, arguments, throwables, null);
        }
        var body = parseBody(sc);
        return new ClassElement.Method(type, name.value(), modifiers, arguments, throwables, body);
    }

    public List<Expression.Variable> parseArguments(Scanner sc) {
        sc.takeStringOrThrow("(");
        var variables = new ArrayList<Expression.Variable>();
        var variable = variableParser.parse(sc, null);
        if (variable != null) {
            variables.add(variable);
        }
        while (!sc.takeString(")")) {
            sc.takeStringOrThrow(",");
            variable = variableParser.parse(sc, null);
            variables.add(variable);
        }
        return variables;
    }

    public List<TypeRef> parseThrows(Scanner sc) {
        if (!sc.takeString("throws")) {
            sc.dropWhitespaces();
            if (sc.charAt(0) == ';') {
                return List.of();
            }
            sc.takeStringOrThrow("{");
            return List.of();
        }
        var throwables = new ArrayList<TypeRef>();
        var throwable1 = typeParser.tryParse(sc);
        if (throwable1 == null) {
            throw new ParseException("Throwable expected");
        }
        throwables.add(throwable1);
        while (!sc.takeString("{")) {
            sc.takeStringOrThrow(",");
            var throwable = typeParser.tryParse(sc);
            if (throwable == null) {
                throw new ParseException("Throwable expected");
            }
            throwables.add(throwable);
            sc.dropWhitespaces();
            if (sc.charAt(0) == ';') {
                return throwables;
            }
        }
        return throwables;
    }

    public List<Expression> parseBody(Scanner sc) {
        return expressionParser.parseExpressions(sc);
    }
}
