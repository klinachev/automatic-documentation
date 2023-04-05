package ru.itmo.ad.parser.java.expression;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.classes.Variable;
import ru.itmo.ad.parser.java.classes.VariableParser;
import ru.itmo.ad.parser.java.comment.CommentParser;
import ru.itmo.ad.parser.java.object.Object;
import ru.itmo.ad.parser.java.object.*;
import ru.itmo.ad.parser.java.types.TypeParser;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private final TypeParser typeParser = new TypeParser();
    private final ObjectParser objectParser = new ObjectParser();
    private final VariableParser variableParser = new VariableParser();
    private final CommentParser commentParser = new CommentParser();

//    public String parse(Scanner sc) {
//        int i = sc.loadUntil(';');
//        var result = sc.getString().substring(0, i);
//        sc.dropUntil(i + 1);
//        return result;
//    }

    public List<String> parseExpressions(Scanner sc) {
        var list = new ArrayList<String>();
        for (; !sc.takeString("}"); commentParser.parse(sc)) {
            parseExpression(sc, list);
        }
        return list;
    }

    private void parseExpression(Scanner sc, ArrayList<String> list) {
        commentParser.parse(sc);
        if (sc.takeWord("throw")) {
            list.add(parseStatement(sc).toString());
            sc.takeStringOrThrow(";");
        } else if (sc.takeWord("return")) {
            if (sc.takeString(";")) {
                return;
            }
            list.add(parseStatement(sc).toString());
            sc.takeStringOrThrow(";");
        } else if (sc.takeWord("if")) {
            sc.takeStringOrThrow("(");
            parseStatement(sc);
            sc.takeStringOrThrow(")");
            if (sc.takeString("{")) {
                parseExpressions(sc);
            } else {
                parseExpression(sc, list);
            }
            if (sc.takeWord("else")) {
                if (sc.takeString("{")) {
                    parseExpressions(sc);
                } else {
                    parseExpression(sc, list);
                }
            }
        } else if (sc.takeWord("while")) {
            sc.takeStringOrThrow("(");
            parseStatement(sc);
            sc.takeStringOrThrow(")");
            sc.takeStringOrThrow("{");
            parseExpressions(sc);
        } else if (sc.takeWord("for")) {
            parseFor(sc);
            sc.takeStringOrThrow("{");
            parseExpressions(sc);
        } else if (sc.takeWord("try")) {
            if (sc.takeString("(")) {
                while (!sc.takeString(")")) {
                    Variable variable = variableParser.parse(sc, null);
                    sc.takeStringOrThrow("=");
                    parseStatement(sc);
                }
            }
            sc.takeStringOrThrow("{");
            parseExpressions(sc);
            while (sc.takeWord("catch")) {
                sc.takeStringOrThrow("(");
                variableParser.parse(sc, null);
                sc.takeStringOrThrow(")");
                sc.takeStringOrThrow("{");
                parseExpressions(sc);
            }
            if (sc.takeString("finally")) {
                sc.takeStringOrThrow("{");
                parseExpressions(sc);
            }
        } else if (sc.takeString("switch")) {
            sc.takeString("(");
            parseStatement(sc);
            sc.takeStringOrThrow(")");
            sc.takeStringOrThrow("{");
            while (sc.takeString("case")) {
                String type = typeParser.tryParse(sc);
                if (type != null) {
                    DefaultObject name = objectParser.tryTakeName(sc);
                } else {
                    Object object = objectParser.tryTakeObject(sc);
                }
                if (sc.takeString("->")) {
                    if (sc.takeString("{")) {
                        parseExpressions(sc);
                    } else {
                        parseStatement(sc);
                        sc.takeStringOrThrow(";");
                    }
                }
            }
            if (sc.takeString("default")) {
                if (sc.takeString("->")) {
                    if (sc.takeString("{")) {
                        parseExpressions(sc);
                    } else {
                        parseStatement(sc);
                        sc.takeStringOrThrow(";");
                    }
                }
            }
        } else {
            var start = typeParser.tryParse(sc);
            isPostfixOperator(sc);
            if (sc.takeString(";")) {
                return;
            }
            if (sc.takeString("(")) { // is function
                List<Object> objects = parseFunctionCall(sc);
                sc.takeStringOrThrow(";");
                return;
            }
            if (isReassign(sc)) { // is reassign
                var name = start;
                parseStatement(sc);
                sc.takeStringOrThrow(";");
                return;
            }
            var variable = objectParser.tryTakeName(sc); // new variable
            if (variable == null) {
                throw new ParseException(sc.getString());
            }
            if (!sc.takeString("=")) {
                sc.takeStringOrThrow(";");
                return;
            }
            String expression = parseStatement(sc).toString();
            sc.takeStringOrThrow(";");
            list.add(variable.value() + expression);
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

    public Object parseStatement(Scanner sc) {
        isPrefixOperator(sc);
        Object object;
        if (sc.takeString("{")) {
            object = tryTakeArray(sc);
        } else {
            boolean constructor = sc.takeString("new ");
            String value = typeParser.tryParse(sc);
            object = new DefaultObject(value);
            if (constructor) {
                if (sc.takeString("(")) { // if not array
                    object = new FunctionCall(object, parseFunctionCall(sc), true);
                } else if (sc.takeString("{")) { // if array
                    tryTakeArray(sc);
                }
            } else {
                if (value != null) { // function call
                    object = new DefaultObject(value);
                    if (sc.takeString("(")) {
                        object = new FunctionCall(object, parseFunctionCall(sc), false);
                    }
                } else {
                    object = objectParser.tryTakeObject(sc);
                }
            }
        }
        sc.loadWhileWhitespaces();
        var operator = isBinaryOperator(sc);
        if (operator != null) {
            object = new Operator(object, parseStatement(sc), "" + operator);
        }
        if (sc.takeString("?")) {
            var ifTrue = parseStatement(sc);
            sc.takeStringOrThrow(":");
            var ifFalse = parseStatement(sc);
        }
        if (sc.takeString("instanceof")) {
            var type = objectParser.tryTakeName(sc);
            var name = objectParser.tryTakeName(sc);
            if (type == null) {
                throw new ParseException("type expected");
            }
        }
        isPostfixOperator(sc);
        return object;
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

    public List<Object> parseFunctionCall(Scanner sc) {
        var args = new ArrayList<Object>();
        if (!sc.takeString(")")) {
            args.add(parseStatement(sc));
            while (!sc.takeString(")")) {
                sc.takeStringOrThrow(",");
                args.add(parseStatement(sc));
            }
        }
        if (sc.takeString(".")) {
            var functionName = typeParser.tryParse(sc);
            sc.takeString("(");
            parseFunctionCall(sc);
//            object = new FunctionCall(object, parseFunctionCall(sc), constructor);
        }
        return args;
    }

    public ArrayObject tryTakeArray(Scanner sc) {
        sc.loadWhileWhitespaces();
        var list = new ArrayList<Object>();
        while (!sc.takeString("}")) {
            var object = parseStatement(sc);
            list.add(object);
            sc.takeString(",");
        }
        return new ArrayObject(list);
    }
}
