package ru.itmo.ad.calculation;

import ru.itmo.ad.FileWithPath;
import ru.itmo.ad.parser.java.JavaFile;
import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.expression.Expression;
import ru.itmo.ad.parser.java.types.TypeRef;

import java.util.*;
import java.util.stream.Collectors;

public class TerminalStatesResolver {

    private final ImportResolver importResolver;
    private Map<String, TypeRef> returnTypes;
    private Map<String, List<TypeRef>> variables;
    private FileWithPath currentFile;
    private ClassElement.Method currentMethod;
    private int tryLevel = 0;

    public TerminalStatesResolver(ImportResolver importResolver) {
        this.importResolver = importResolver;
    }

    public void calculate(List<FileWithPath> files) {
        returnTypes = files.stream()
                .flatMap(fileWithPath -> fileWithPath.javaFile()
                        .mainClass().methods().stream()
                        .map(method -> new MethodInfo(
                                method,
                                fileWithPath.javaFile().mainClass(),
                                fileWithPath.javaFile()
                        )))
                .collect(Collectors.toMap(
                        info -> methodId(
                                info.javaFile,
                                info.classElement,
                                info.method.name(),
                                info.method.arguments().size()),
                        info -> info.method.type()
                ));
        for (var file : files) {
            currentFile = file;
            ClassElement.Class mainClass = file.javaFile().mainClass();
            var globalVars = mainClass.fields().stream().collect(Collectors.toMap(
                    field -> field.variable().name(),
                    field -> new ArrayList<>(List.of(field.variable().type()))
            ));
            for (var method : mainClass.methods()) {
                currentMethod = method;
                variables = new HashMap<>(globalVars);
                if (method.body() != null) {
                    resolve(method.body());
                }
            }
        }
    }

    public static String methodId(
            JavaFile javaFile,
            ClassElement.Class classElement,
            String name,
            int size) {
        return javaFile.packageName() + classElement.type().name() + name + size;
    }

    public static String methodIdByNames(
            JavaFile javaFile,
            ClassElement.Class classElement,
            String name,
            List<String> args) {
        return javaFile.packageName() + classElement.type().name() + name
                + String.join("", args);
    }

    public void resolve(List<Expression> body) {
        Set<String> names = new HashSet<>();
        for (var expr : body) {
            switch (expr) {
                case Expression.Assign assign -> {
                    if (assign.variable().type().name().equals("var")) {
                        TypeRef typeRef = resolveBodyStatement(assign.body());
                        resolveVariable(names, new Expression.Variable(typeRef, assign.variable().name(), null));
                    } else {
                        resolveVariable(names, assign.variable());
                    }
                }
                case Expression.Block block -> resolve(block.body());
                case Expression.ExpressionList expressionList -> resolve(expressionList.body());
                case Expression.If anIf -> {
                    resolve(anIf.thenBranch());
                    resolve(anIf.elseBranch());
                }
                case Expression.Variable variable -> resolveVariable(names, variable);
                case Expression.BinaryOperator binaryOperator -> {
                }
                case Expression.ArrayObject arrayObject -> {
                }
                case Expression.FunctionCall functionCall -> {
                }
                case Expression.Object object -> {
                }
                case Expression.Reassign reassign -> {
                }
                case Expression.Terminal terminal -> {
                    var returnClass = importResolver.resolveClass(currentFile, currentMethod.type().name());
                    if (returnClass != null) {
                        if (terminal.body() instanceof Expression.Name name) {
                            if (name.name().equals("null")) {
                                addInfo(terminal, new TypeRef("null", List.of(), false), currentMethod.returnsInfo());
                            } else {
                                addInfo(terminal, returnClass.type(), currentMethod.returnsInfo());
                            }
                        } else {
                            addInfo(terminal, returnClass.type(), currentMethod.returnsInfo());
                        }
                    }
                    if (terminal.name().equals("throw") && tryLevel == 0) {
                        TypeRef typeRef = resolveBodyStatement(terminal.body());
                        addInfo(terminal, typeRef, currentMethod.throwsInfo());
                    }
                }
                case Expression.UnaryOperator unaryOperator -> {
                }
                case Expression.Constructor constructor -> {
                }
                case Expression.Name name -> {
                }
                case Expression.Try aTry -> {
                    tryLevel++;
                    resolve(aTry.body());
                    tryLevel--;
                    resolve(aTry.catchBlock());
                }
            }
        }
        for (var name : names) {
            List<TypeRef> typeRefs = variables.get(name);
            typeRefs.remove(typeRefs.size() - 1);
        }
    }

    private void addInfo(Expression.Terminal terminal, TypeRef name, Map<TypeRef, List<String>> map) {
        List<String> strings = map.get(name);
        if (strings == null) {
            map.put(name, new ArrayList<>(terminal.comments()));
            return;
        }
        strings.addAll(terminal.comments());
    }

    public TypeRef resolveBodyStatement(Expression expression) {
        switch (expression) {
            case Expression.FunctionCall functionCall -> {
                String id;
                if (functionCall.receiver() == null) {
                    id = methodId(
                            currentFile.javaFile(),
                            currentFile.javaFile().mainClass(),
                            functionCall.name().name(),
                            functionCall.args().size()
                    );
                } else {
                    TypeRef typeRef = resolveBodyStatement(functionCall.receiver());
                    if (typeRef == null) {
                        return null;
                    }
                    FileWithPath fileWithPath = importResolver.resolveFile(currentFile, typeRef.name());
                    if (fileWithPath == null) {
                        return null;
                    }
                    id = methodId(
                            fileWithPath.javaFile(),
                            fileWithPath.javaFile().mainClass(),
                            functionCall.name().name(),
                            functionCall.args().size()
                    );
                }
                return returnTypes.get(id);
            }
            case Expression.Constructor constructor -> {
                return constructor.name();
            }
            case Expression.Name name -> {
                List<TypeRef> typeRefs = variables.get(name.name());
                if (typeRefs == null) {
                    return null;
                }
                return typeRefs.get(typeRefs.size() - 1);
            }
            default -> throw new IllegalStateException();
        }
    }

    private void resolveVariable(Set<String> names, Expression.Variable variable) {
        var variableList = variables.get(variable.name());
        names.add(variable.name());
        if (variableList != null) {
            variableList.add(variable.type());
        } else {
            List<TypeRef> list = new ArrayList<>();
            list.add(variable.type());
            variables.put(variable.name(), list);
        }
    }

//    public void getVariableValues(TerminalInfo terminal) {
//        switch (terminal.terminal.body()) {
//            case Expression.FunctionCall functionCall -> {
//                if (functionCall.constructor()) {
//                    return functionCall;
//                }
//            }
//            case Expression.Object object -> {
//            }
//            default -> throw new IllegalStateException();
//        }
//    }
//
//    public Stream<Expression.Terminal> getAllTerminals(Collection<Expression> expressions) {
//        return expressions.stream().flatMap(expression -> switch (expression) {
//                    case Expression.If iff -> Stream.concat(
//                            getAllTerminals(iff.thenBranch()),
//                            getAllTerminals(iff.elseBranch()));
//                    case Expression.ArrayObject arrayObject -> getAllTerminals(arrayObject.objects());
//                    case Expression.Block block -> getAllTerminals(block.body());
//                    case Expression.ExpressionList expressionList -> getAllTerminals(expressionList.body());
//                    case Expression.Terminal terminal -> Stream.of(terminal);
//                    default -> Stream.empty();
//                }
//        );
//    }

    record MethodInfo(
            ClassElement.Method method,
            ClassElement.Class classElement,
            JavaFile javaFile
    ) {
    }

    record TerminalInfo(
            String methodId,
            Expression.Terminal terminal,
            ClassElement.Method method,
            ClassElement.Class classElement,
            List<TypeRef> terminals
    ) {
    }
}
