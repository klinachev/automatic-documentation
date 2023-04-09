package ru.itmo.ad.parser.java.classes;

import ru.itmo.ad.parser.java.annotation.Annotation;
import ru.itmo.ad.parser.java.expression.Expression;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.types.TypeRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public sealed interface ClassElement permits ClassElement.Class, ClassElement.Field, ClassElement.Method {
    List<Annotation> annotations();

    List<String> comments();

    record Method(
            TypeRef type,
            String name,
            Modifiers modifiers,
            List<Expression.Variable> arguments,
            List<TypeRef> throwables,
            List<Expression> body,
            List<Annotation> annotations,
            List<String> comments,
            Map<TypeRef, List<String>> throwsInfo,
            Map<TypeRef, List<String>> returnsInfo
    ) implements ClassElement {
        public Method(TypeRef type,
                      String name,
                      Modifiers modifiers,
                      List<Expression.Variable> arguments,
                      List<TypeRef> throwables,
                      List<Expression> body) {
            this(type, name, modifiers, arguments, throwables,
                    body, new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
        }
    }

    record Field(
            Expression.Variable variable,
            List<Annotation> annotations,
            List<String> comments
    ) implements ClassElement {
        public Field(Expression.Variable variable) {
            this(variable, new ArrayList<>(), new ArrayList<>());
        }
    }

    record Class(
            TypeRef type,
            Modifiers modifiers,
            List<Annotation> annotations,
            List<String> comments,
            List<ClassElement.Field> fields,
            List<ClassElement.Method> constructors,
            List<ClassElement.Method> methods,
            List<ClassElement.Class> classes,
            Inheritance inheritance
    ) implements ClassElement {

        public Class(TypeRef name, Modifiers modifiers, Inheritance inheritance) {
            this(name, modifiers, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    inheritance);
        }

        public record Inheritance(TypeRef extend, List<TypeRef> implement, List<TypeRef> permits) {

        }
    }
}
