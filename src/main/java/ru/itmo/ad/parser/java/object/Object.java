package ru.itmo.ad.parser.java.object;

public sealed interface Object
        permits Object.CharObject, DefaultObject, Object.NumberObject, Object.StringObject {
    record CharObject(char value) implements Object {
    }

    record NumberObject(String value) implements Object {
    }

    record StringObject(String value) implements Object {
    }
}
