package ru.itmo.ad.parser.java.object;

public sealed interface Object
        permits ArrayObject, CharObject, DefaultObject, FunctionCall, NumberObject, Operator, StringObject {
}
