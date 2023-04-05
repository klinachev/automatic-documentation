package ru.itmo.ad.generator.html;

import j2html.rendering.IndentedHtml;
import j2html.tags.DomContent;
import j2html.tags.specialized.*;
import ru.itmo.ad.DocumentationApi;
import ru.itmo.ad.generator.html.template.HtmlTemplateCreator;
import ru.itmo.ad.parser.java.JavaFile;
import ru.itmo.ad.parser.java.classes.ClassElement;
import ru.itmo.ad.parser.java.imports.ImportResolver;
import ru.itmo.ad.parser.java.modifiers.Modifiers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static j2html.TagCreator.*;

public class FileGenerator {
    private final HtmlTemplateCreator templateCreator;

    private final ImportResolver importResolver;

    public FileGenerator(ImportResolver importResolver, Path root) {
        this.importResolver = importResolver;
        this.templateCreator = new HtmlTemplateCreator(root);
    }

    public void generate(
            DocumentationApi.FileWithPath file
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
                                        elementName(clazz.name())
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

    private static DivTag classHeader(JavaFile javaFile, ClassElement.Class clazz) {
        return div(
                div(span("Package").withClass("module-label-in-type"),
                        a(javaFile.packageName()).withHref("")).withClass("sub-title"),
                h1("Class " + clazz.name()).withClass("title")
        ).withClass("header");
    }

    private SectionTag methodDetails(DocumentationApi.FileWithPath file, ClassElement.Class clazz) {
        return section(
                h2("Method Details"),
                ul(each(clazz.methods(), method ->
                                li(section(
                                        h3(method.name()),
                                        div(
                                                modifiers(method.modifiers()),
                                                span(
                                                        resolveHref( file, method)
                                                ).withClass("return-type"),
                                                elementName(method.name())
                                        ).withClass("member-signature"),
                                        div(
                                                String.join("\n", method.comments())
                                        ).withClass("block")
                                ).withClass("detail"))
                )).withClass("member-list")
        ).withClass("method-details");
    }

    private ATag resolveHref(DocumentationApi.FileWithPath file, ClassElement.Method method) {
        ATag a = a(method.type());
        Path resolve = importResolver.resolve(file, method.type());
        if (resolve != null) {
            return a.withHref(file.destination().relativize(resolve).toString());
        }
        return a;
    }

    private static SpanTag elementName(String name) {
        return span(name).withClass("element-name type-name-label");
    }

    private static SpanTag modifiers(Modifiers modifiers) {
        return span(modifiers.toString()).withClass("modifiers");
    }
}
