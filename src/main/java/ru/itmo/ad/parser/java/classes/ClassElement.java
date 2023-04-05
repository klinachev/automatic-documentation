package ru.itmo.ad.parser.java.classes;

import ru.itmo.ad.parser.java.annotation.Annotation;
import ru.itmo.ad.parser.java.modifiers.Modifiers;

import java.util.ArrayList;
import java.util.List;

public sealed interface ClassElement permits ClassElement.Class, ClassElement.Field, ClassElement.Method {
    List<Annotation> annotations();
    List<String> comments();
    record Method(
            String type,
            String name,
            Modifiers modifiers,
            List<Variable> arguments,
            List<String> throwables,
            List<String> body,
            List<Annotation> annotations,
            List<String> comments
    ) implements ClassElement {
        public Method(String type,
                      String name,
                      Modifiers modifiers,
                      List<Variable> arguments,
                      List<String> throwables,
                      List<String> body) {
            this(type, name, modifiers, arguments, throwables,
                    body, new ArrayList<>(), new ArrayList<>());
        }
    }

    record Field(
            Variable variable,
            List<Annotation> annotations,
            List<String> comments
    ) implements ClassElement {
        public Field(Variable variable) {
            this(variable, new ArrayList<>(), new ArrayList<>());
        }
    }

    record Class(
            String name,
            Modifiers modifiers,
            List<Annotation> annotations,
            List<String> comments,
            List<ClassElement.Field> fields,
            List<ClassElement.Method> methods,
            List<ClassElement.Class> classes
    ) implements ClassElement {

        public Class(String name, Modifiers modifiers) {
            this(name, modifiers, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }
}
