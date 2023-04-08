package ru.itmo.ad.parser.java.object;

import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.expression.Expression;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;

public class ObjectParser {

    public Object.StringObject tryTakeJavaString(Scanner sc) {
        sc.loadWhileWhitespaces();
        if (!sc.takeString("\"")) {
            return null;
        }
        char ch = '"';
        var sb = new StringBuilder().append('"');
        for (int i = 0; ; ++i) {
            int load = sc.load(i + 1);
            if (sc.charAt(i) == '"' && ch != '\\') {
                sc.dropUntil(i + 1);
                return new Object.StringObject(sb.append('"').toString());
            }
            ch = sc.charAt(i);
            if (ch == '\\') {
                ch = sc.charAt(++i);
            }
            sb.append(ch);
        }
    }

    public Object.CharObject tryTakeChar(Scanner sc) {
        sc.loadWhileWhitespaces();
        if (!sc.takeString("'")) {
            return null;
        }
        int pos = sc.loadUntil('\'');
        char c = sc.charAt(0);
        if (c == '\\') {
            c = sc.charAt(1);
            sc.dropUntil(3);
            return new Object.CharObject(c);
        }
        if (pos != 1) {
            throw new ParseException("Character expected");
        }
        sc.dropUntil(2);
        return new Object.CharObject(c);
    }

    public int numberPos(Scanner sc, int i) {
        sc.loadWhileWhitespaces();
        if (!Character.isDigit(sc.charAt(i))) {
            return -1;
        }
        i++;
        while (Character.isAlphabetic(sc.charAt(i)) || Character.isDigit(sc.charAt(i))
                || sc.charAt(i) == '.' || sc.charAt(i) == '_') {
            i++;
        }
        return i;
    }

    public Object.NumberObject tryTakeNumber(Scanner sc) {
        var pos = numberPos(sc, 0);
        if (pos == -1) {
            return null;
        }
        var number = sc.getString().substring(0, pos);
        sc.dropUntil(pos);
        return new Object.NumberObject(number);
    }

    public DefaultObject tryTakeName(Scanner sc) {
        sc.loadWhileWhitespaces();
        if (!Character.isAlphabetic(sc.charAt(0))) {
            return null;
        }
        sc.loadUntilSpace();
        int i = 0;
        while (Character.isAlphabetic(sc.charAt(i)) || Character.isDigit(sc.charAt(i))
                || sc.charAt(i) == '.' || sc.charAt(i) == '_'
                || sc.charAt(i) == '[' || sc.charAt(i) == ']'
                || sc.charAt(i) == ':') {
            i++;
        }
        var number = sc.getString().substring(0, i);
        sc.dropUntil(i);
        return new DefaultObject(number);
    }

    public Expression.ArrayObject tryTakeArray(Scanner sc) {
        sc.loadWhileWhitespaces();
        if (!sc.takeString("{")) {
            return null;
        }
        var list = new ArrayList<Expression>();
        while (!sc.takeString("}")) {
            var object = tryTakeObject(sc);
            list.add(new Expression.Object(object));
            sc.takeString(",");
        }
        return new Expression.ArrayObject(list);
    }

    public Object tryTakeObject(Scanner sc) {
//        var array = tryTakeArray(sc);
//        if (array != null) {
//            return array;
//        }
        var number = tryTakeNumber(sc);
        if (number != null) {
            return number;
        }
        var character = tryTakeChar(sc);
        if (character != null) {
            return character;
        }
        var string = tryTakeJavaString(sc);
        if (string != null) {
            return string;
        }
        var defaultObject = tryTakeName(sc);
        if (defaultObject != null) {
            return defaultObject;
        }
        return null;
    }

}
