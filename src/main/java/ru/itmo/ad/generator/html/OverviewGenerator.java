package ru.itmo.ad.generator.html;

import j2html.rendering.IndentedHtml;
import j2html.tags.DomContent;
import ru.itmo.ad.DocumentationApi;
import ru.itmo.ad.generator.html.template.HtmlTemplateCreator;
import ru.itmo.ad.parser.java.imports.ImportResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static j2html.TagCreator.*;

public class OverviewGenerator {

    private final HtmlTemplateCreator htmlTemplateCreator;

    private final ImportResolver importResolver;
    private final Path root;

    public OverviewGenerator(ImportResolver importResolver, Path root) {
        this.importResolver = importResolver;
        this.root = root;
        this.htmlTemplateCreator = new HtmlTemplateCreator(root);
    }

    public void generate(List<DocumentationApi.FileWithPath> files) throws IOException {
        var path = root.resolve("Overview.html");
        DomContent[] summaries = {section(ul(
                each(files, file -> li(
                        a(file.javaFile().fullName())
                                .withHref(root.relativize(file.destination()).toString())
                ))).withClass("summary-list")).withClass("summary")};

        var html = htmlTemplateCreator.generate("Overview", path, summaries);
        try (var ignored = html.render(IndentedHtml.into(
                Files.newBufferedWriter(path)))) {
        }
        System.out.println(path + " generated.");
    }
}
