package ru.itmo.ad.parser.java.classes;

import ru.itmo.ad.parser.java.modifiers.Modifiers;

public record Variable(String type, String name, Modifiers modifiers) {
}
