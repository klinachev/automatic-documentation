package ru.itmo.ad.parser.java.expression;


import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.types.TypeRef;

import java.util.List;

public sealed interface Expression {
    sealed interface Statement extends Expression {

    }

    record Terminal(String name, Expression body, List<String> comments) implements Expression {
    }

    record Try(
            List<Expression> resources,
            List<Expression> body,
            List<Expression> catchBlock
    ) implements Expression {

    }
    record ExpressionList(List<Expression> body) implements Expression {

    }

    record Block(String name, List<Expression> body) implements Expression {
    }

    record If(Expression condition, List<Expression> thenBranch, List<Expression> elseBranch) implements Expression {
    }

    record Assign(Variable variable, Expression body) implements Expression {
    }

    record Reassign(String variable, Expression body) implements Expression {
    }

    record UnaryOperator(String operator, Expression expression) implements Statement {
    }

    record BinaryOperator(String operator, Expression left, Expression right) implements Statement {
    }

    record Object(ru.itmo.ad.parser.java.object.Object object) implements Statement {

    }

    record Name(String name) implements Statement {

    }

    record ArrayObject(List<Expression> objects) implements Statement {
    }

    record Constructor(
            TypeRef name,
            List<Expression> args
    ) implements Statement {
    }

    record FunctionCall(
            Statement receiver,
            TypeRef name,
            List<Expression> args
    ) implements Statement {
    }

    record Variable(TypeRef type, String name, Modifiers modifiers) implements Expression {
    }

}
