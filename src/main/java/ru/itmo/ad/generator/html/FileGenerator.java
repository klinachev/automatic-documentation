package ru.itmo.ad.generator.html;

import j2html.rendering.IndentedHtml;
import j2html.tags.DomContent;
import j2html.tags.specialized.*;
import ru.itmo.ad.FileWithPath;
import ru.itmo.ad.calculation.ImportResolver;
import ru.itmo.ad.generator.html.template.HtmlTemplateCreator;
import ru.itmo.ad.parser.java.JavaFile;
import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.modifiers.Modifiers;
import ru.itmo.ad.parser.java.types.TypeRef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class FileGenerator {
    private final HtmlTemplateCreator templateCreator;

    private final ImportResolver importResolver;

    public FileGenerator(ImportResolver importResolver, Path root) {
        this.importResolver = importResolver;
        this.templateCreator = new HtmlTemplateCreator(root);
    }

    public void generate(
            FileWithPath file
    ) throws IOException {
        JavaFile javaFile = file.javaFile();
        var clazz = javaFile.classes().get(0);
        HtmlTag html = templateCreator.generate(
                javaFile.name(),
                file.destination(),
                new DomContent[]{
                        classHeader(javaFile, clazz),
                        section(
                                hr(),
                                div(
                                        modifiers(clazz.modifiers()),
                                        elementName(clazz.type().toString()),
                                        inheritance(file, clazz)
                                ).withClass("type-signature"),
                                div(String.join("\n", clazz.comments()))
                                        .withClass("block"),
                                section(
                                ).withClass("summary"),
                                section(ul(li(
                                        methodDetails(file, clazz)
                                )).withClass("details-list")).withClass("details")
                        ).withClass("class-description")}
        );
        Path parent = file.destination().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (var ignored = html.render(IndentedHtml.into(
                Files.newBufferedWriter(file.destination())))) {
        }
        System.out.println(file.destination() + " generated.");
    }

    public DomContent inheritance(FileWithPath file, ClassElement.Class classElement) {
        List<DomContent> list = new ArrayList<>();
        var inheritance = classElement.inheritance();
        if (inheritance.extend() != null) {
            list.add(div("extends"));
            list.add(resolveHref(file, inheritance.extend()));
        }
        addInfo(file, list, inheritance.implement(), "implements");
        addInfo(file, list, inheritance.permits(), "permits");
        DomContent[] array = new DomContent[list.size()];
        list.toArray(array);
        return span(array).withClass("extends-implements");
    }

    private void addInfo(
            FileWithPath file,
            List<DomContent> list,
            List<TypeRef> typeRefs,
            String name
    ) {
        if (typeRefs != null && !typeRefs.isEmpty()) {
            list.add(div(name));
            list.add(each(typeRefs, permit -> resolveHref(file, permit)));
        }
    }

    private static DivTag classHeader(JavaFile javaFile, ClassElement.Class clazz) {
        return div(
                div(span("Package").withClass("module-label-in-type"),
                        a(javaFile.packageName()).withHref("")).withClass("sub-title"),
                h1("Class " + clazz.type()).withClass("title")
        ).withClass("header");
    }

    private SectionTag methodDetails(FileWithPath file, ClassElement.Class clazz) {
        return section(
                h2("Method Details"),
                ul(each(clazz.methods(), method ->
                        li(onSameLine(section(
                                h3(method.name()),
                                div(
                                        modifiers(method.modifiers()),
                                        span(
                                                resolveHref(file, method.type())
                                        ).withClass("return-type"),
                                        rawHtml("&nbsp;"),
                                        elementName(method.name()),
                                        wbr(),
                                        span(text("("),
                                                rawHtml(method.arguments().stream()
                                                        .map(arg -> each(resolveHref(file, arg.type()), rawHtml("&nbsp;" + arg.name())).toString())
                                                        .collect(Collectors.joining(", "))),
                                                text(")")
                                        ).withClass("parameters"),
                                        throwsBlock(file, method)
                                ).withClass("member-signature"),
                                div(
                                        rawHtml(String.join("<br>", method.comments()))
                                ).withClass("block"),
                                dl(
                                        throwsInfo(file, method)
                                ).withClass("notes")
                        ).withClass("detail")))
                )).withClass("member-list")
        ).withClass("method-details");
    }

    private DomContent onSameLine(DomContent... domContents) {
        return rawHtml(each(domContents).render());
    }

    private DomContent throwsInfo(FileWithPath file, ClassElement.Method method) {
        if (method.throwsInfo().isEmpty()) {
            return rawHtml("");
        }
        return each(dt("Throws:"), each(method.throwsInfo(), thr ->
                dd(resolveHref(file, thr.type()), (rawHtml(" - " + String.join(" ", thr.comments()))))));
    }

    private DomContent throwsBlock(FileWithPath file, ClassElement.Method method) {
        if (method.throwables().isEmpty()) {
            return rawHtml("");
        }
        return span(each(text(" throws "), each(method.throwables(), thr ->
                resolveHref(file, thr))))
                .withClass("exceptions");
    }

    private ATag resolveHref(FileWithPath file, TypeRef typeRef) {
        Path resolve = importResolver.resolve(file, typeRef.name());
        ATag a = a(typeRef.toString());
        if (resolve != null && file.destination().getParent() != null) {
            a = a.withHref(file.destination().getParent().relativize(resolve).toString());
        }
        return a;
    }

    private static SpanTag elementName(String name) {
        return span(name).withClass("element-type");
    }

    private static SpanTag modifiers(Modifiers modifiers) {
        return span(modifiers.toString()).withClass("modifiers");
    }
}
