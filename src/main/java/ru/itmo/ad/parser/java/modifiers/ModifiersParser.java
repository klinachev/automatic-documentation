package ru.itmo.ad.parser.java.modifiers;

import ru.itmo.ad.parser.java.utils.Scanner;

public class ModifiersParser {

    public Modifiers parse(Scanner sc) {
        var privacy = Modifiers.Privacy.DEFAULT;
        var isFinal = false;
        var isStatic = false;
        Modifiers.ClassType classType = null;
        var isSealed = false;
        var isDefault = false;
        while (true) {
            int pos = sc.loadUntilSpace();
            if (pos == -1) {
                return null;
            }
            var s = sc.getString().substring(0, pos);
            var newPrivacy = getPrivacy(s);
            if (newPrivacy != null) {
                privacy = newPrivacy;
                sc.dropUntil(pos);
                continue;
            }
            var newClassType = getClassType(s);
            if (newClassType != null) {
                classType = newClassType;
                sc.dropUntil(pos);
                continue;
            }
            switch (s) {
                case "static" -> isStatic = true;
                case "final" -> isFinal = true;
                case "sealed" -> isSealed = true;
                case "default" -> isDefault = true;
                default -> {
                    return new Modifiers(isFinal, isStatic, privacy, isDefault, classType, isSealed);
                }
            }
            sc.dropUntil(pos);
        }
    }

    private static Modifiers.ClassType getClassType(String s) {
        for (var value : Modifiers.ClassType.values()) {
            if (value.getValue().equals(s)) {
                return value;
            }
        }
        return null;
    }

    private static Modifiers.Privacy getPrivacy(String s) {
        for (var value : Modifiers.Privacy.values()) {
            if (value.getValue().equals(s)) {
                return value;
            }
        }
        return null;
    }
}
