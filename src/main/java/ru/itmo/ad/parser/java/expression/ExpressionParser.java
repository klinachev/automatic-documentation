package ru.itmo.ad.parser.java.expression;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.classes.VariableParser;
import ru.itmo.ad.parser.java.comment.CommentParser;
import ru.itmo.ad.parser.java.modifiers.ModifiersParser;
import ru.itmo.ad.parser.java.object.DefaultObject;
import ru.itmo.ad.parser.java.object.Object;
import ru.itmo.ad.parser.java.object.ObjectParser;
import ru.itmo.ad.parser.java.types.TypeParser;
import ru.itmo.ad.parser.java.types.TypeRef;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private final TypeParser typeParser = new TypeParser();
    private final ModifiersParser modifiersParser = new ModifiersParser();
    private final ObjectParser objectParser = new ObjectParser();
    private final VariableParser variableParser = new VariableParser(typeParser);
    private final CommentParser commentParser = new CommentParser();

    public List<Expression> parseExpressions(Scanner sc) {
        var list = new ArrayList<Expression>();
        while (!sc.takeString("}")) {
            list.add(parseExpression(sc));
            commentParser.parseAll(sc);
        }
        return list;
    }

    private Expression parseExpression(Scanner sc) {
        List<String> comments = commentParser.parseAll(sc);
        if (sc.takeWord("throw")) {
            var expression = parseStatement(sc);
            sc.takeStringOrThrow(";");
            return new Expression.Terminal("throw", expression, comments);
        } else if (sc.takeWord("return")) {
            if (sc.takeString(";")) {
                return new Expression.Terminal("return", null, comments);
            }
            var expression = parseStatement(sc);
            sc.takeStringOrThrow(";");
            return new Expression.Terminal("return", expression, comments);
        } else if (sc.takeWord("if")) {
            sc.takeStringOrThrow("(");
            var condition = parseStatement(sc);
            sc.takeStringOrThrow(")");
            List<Expression> thenBranch;
            if (sc.takeString("{")) {
                thenBranch = parseExpressions(sc);
            } else {
                thenBranch = List.of(parseExpression(sc));
            }
            List<Expression> elseBranch = List.of();
            if (sc.takeWord("else")) {
                if (sc.takeString("{")) {
                    elseBranch = parseExpressions(sc);
                } else {
                    elseBranch = List.of(parseExpression(sc));
                }
            }
            return new Expression.If(condition, thenBranch, elseBranch);
        } else if (sc.takeWord("while")) {
            sc.takeStringOrThrow("(");
            Expression condition = parseStatement(sc);
            sc.takeStringOrThrow(")");
            sc.takeStringOrThrow("{");
            List<Expression> expressions = parseExpressions(sc);
            return new Expression.Block("while", expressions);
        } else if (sc.takeWord("do")) {
            sc.takeStringOrThrow("{");
            List<Expression> expressions = parseExpressions(sc);
            sc.takeStringOrThrow("while");
            sc.takeStringOrThrow("(");
            Expression condition = parseStatement(sc);
            sc.takeStringOrThrow(")");
            sc.takeStringOrThrow(";");
            return new Expression.Block("do", expressions);
        } else if (sc.takeWord("for")) {
            parseFor(sc);
            sc.takeStringOrThrow("{");
            List<Expression> expressions = parseExpressions(sc);
            return new Expression.Block("for", expressions);
        } else if (sc.takeWord("try")) {
            var resources = new ArrayList<Expression>();
            if (sc.takeString("(")) {
                while (!sc.takeString(")")) {
                    var variable = variableParser.parse(sc, null);
                    sc.takeStringOrThrow("=");
                    Expression expression = parseStatement(sc);
                    resources.add(expression);
                }
            }
            sc.takeStringOrThrow("{");
            var body = parseExpressions(sc);
            var catchBlock = new ArrayList<Expression>();
            while (sc.takeWord("catch")) {
                sc.takeStringOrThrow("(");
                var variable = variableParser.parse(sc, null);
                catchBlock.add(variable);
                sc.takeStringOrThrow(")");
                sc.takeStringOrThrow("{");
                var expressions1 = parseExpressions(sc);
                catchBlock.addAll(expressions1);
            }
            if (sc.takeString("finally")) {
                sc.takeStringOrThrow("{");
                var expressions1 = parseExpressions(sc);
                catchBlock.addAll(expressions1);
            }
            return new Expression.Try(resources, body, catchBlock);
        } else if (sc.takeString("switch")) {
            var list = new ArrayList<Expression>();
            sc.takeString("(");
            var expression = parseStatement(sc);
            list.add(expression);
            sc.takeStringOrThrow(")");
            sc.takeStringOrThrow("{");
            commentParser.parseAll(sc);
            while (sc.takeString("case")) {
                var type = typeParser.tryParse(sc);
                if (type != null) {
                    DefaultObject name = objectParser.tryTakeName(sc);
                    list.add(new Expression.Variable(type, name.value(), null));
                } else {
                    Object object = objectParser.tryTakeObject(sc);
                }
                parseSwitchPart(sc, list);
                commentParser.parseAll(sc);
            }
            if (sc.takeString("default")) {
                parseSwitchPart(sc, list);
            }
            sc.takeStringOrThrow("}");
            return new Expression.ExpressionList(list);
        } else {
            var modifiers = modifiersParser.parse(sc);
            var start = typeParser.tryParse(sc);
            String postfixOperator = isPostfixOperator(sc);
            if (sc.takeString(";")) {
                return new Expression.Object(new DefaultObject(start.name()));
            }
            if (sc.takeString("(")) { // is function
                var functionCall = parseCall(sc, start, false);
                sc.takeStringOrThrow(";");
                return functionCall;
            }
            if (isReassign(sc)) { // is reassign
                Expression expression = parseStatement(sc);
                sc.takeStringOrThrow(";");
                return new Expression.Reassign(start.name(), expression);
            }
            var variable = objectParser.tryTakeName(sc); // new variable
            if (variable == null) {
                throw new ParseException(sc.getString());
            }
            if (!sc.takeString("=")) {
                sc.takeStringOrThrow(";");
                return new Expression.Variable(start, variable.value(), modifiers);
            }
            var expression = parseStatement(sc);
            sc.takeStringOrThrow(";");
            return new Expression.Assign(new Expression.Variable(start, variable.value(), null), expression);
        }
    }

    private void parseSwitchPart(Scanner sc, ArrayList<Expression> list) {
        if (sc.takeString("->")) {
            if (sc.takeString("{")) {
                var expressions = parseExpressions(sc);
                list.addAll(expressions);
            } else {
                var expression1 = parseExpression(sc);
                list.add(expression1);
            }
        }
    }

    private static boolean isReassign(Scanner sc) {
        return sc.takeString("=") || sc.takeString("+=") || sc.takeString("-=") || sc.takeString("*=")
                || sc.takeString("/=");
    }

    private void parseFor(Scanner sc) {
        sc.takeStringOrThrow("(");
        if (!sc.takeString(";")) {
            variableParser.parse(sc, null);
            if (sc.takeString(":")) {
                parseStatement(sc);
                sc.takeStringOrThrow(")");
                return;
            }
            if (sc.takeString("=")) {
                parseStatement(sc);
            }
            sc.takeStringOrThrow(";");
        }
        if (!sc.takeString(";")) {
            parseStatement(sc);
            sc.takeStringOrThrow(";");
        }
        if (!sc.takeString(")")) {
            parseStatement(sc);
            sc.takeStringOrThrow(")");
        }
    }

    public Expression parseStatement(Scanner sc) {
        Expression expression = null;
        if (sc.takeString("(")) {
            expression = parseStatement(sc);
            sc.takeStringOrThrow(")");
        }
        String prefixOperator = isPrefixOperator(sc);
        if (sc.takeString("{")) {
            return tryTakeArray(sc);
        } else {
            boolean constructor = sc.takeString("new ");
            var value = typeParser.tryParse(sc);
            if (sc.takeString("[")) {
                parseStatement(sc);
                sc.takeStringOrThrow("]");
            }
            if (constructor) {
                if (sc.takeString("(")) { // if not array
                    expression = parseCall(sc, value, true);
                } else if (sc.takeString("{")) { // if array
                    return tryTakeArray(sc);
                }
            } else {
                if (value != null) { // function call
                    if (sc.takeString("(")) {
                        expression = parseCall(sc, value, false);
                    } else {
                        expression = new Expression.Name(value.name());
                    }
                } else {
                    expression = new Expression.Object(objectParser.tryTakeObject(sc));
                }
            }
        }
        sc.loadWhileWhitespaces();
        var operator = isBinaryOperator(sc);
        if (operator != null) {
            expression = new Expression.BinaryOperator(operator, expression, parseStatement(sc));
        }
        if (sc.takeString("?")) {
            var ifTrue = parseStatement(sc);
            sc.takeStringOrThrow(":");
            var ifFalse = parseStatement(sc);
            return new Expression.BinaryOperator("?", ifTrue, ifFalse);
        }
        if (sc.takeString("instanceof")) {
            var type = typeParser.tryParse(sc);
            var name = objectParser.tryTakeName(sc);
            if (type == null) {
                throw new ParseException("type expected");
            }
        }
        isPostfixOperator(sc);
        return expression;
    }

    public String firstFound(Scanner sc, List<String> strings) {
        for (var s : strings) {
            if (sc.takeString(s)) {
                return s;
            }
        }
        return null;
    }

    public String isPostfixOperator(Scanner sc) {
        return firstFound(sc, List.of("++", "--"));
    }

    public String isPrefixOperator(Scanner sc) {
        return firstFound(sc, List.of("-", "!", "++", "--", "+"));
    }

    public String isBinaryOperator(Scanner sc) {
        return firstFound(sc, List.of("%", "/", "*", "+", "-", "<",
                ">", "==", "!=", "<=", ">=", "||", "&&"));
    }

    public Expression.Statement parseCall(Scanner sc, TypeRef name, boolean constructor) {
        var args = new ArrayList<Expression>();
        if (!sc.takeString(")")) {
            args.add(parseStatement(sc));
            while (!sc.takeString(")")) {
                sc.takeStringOrThrow(",");
                args.add(parseStatement(sc));
            }
        }
        Expression.Statement result;
        if (constructor) {
            result = new Expression.Constructor(name, args);
        } else {
            int i = name.name().lastIndexOf(".");
            if (i != -1) {
                var receiver = new Expression.Name(name.name().substring(0, i));
                name = new TypeRef(name.name().substring(i + 1), name.templates(), false);
                result = new Expression.FunctionCall(receiver, name, args);
            } else {
                result = new Expression.FunctionCall(null, name, args);
            }
        }
        if (sc.takeString(".")) {
            var functionName = typeParser.tryParse(sc);
            sc.takeString("(");
            var func = (Expression.FunctionCall) parseCall(sc, functionName, false);
            result = new Expression.FunctionCall(result, func.name(), func.args());
        }
        return result;
    }

    public Expression.ArrayObject tryTakeArray(Scanner sc) {
        sc.loadWhileWhitespaces();
        var list = new ArrayList<Expression>();
        while (!sc.takeString("}")) {
            var object = parseStatement(sc);
            list.add(object);
            sc.takeString(",");
        }
        return new Expression.ArrayObject(list);
    }
}
