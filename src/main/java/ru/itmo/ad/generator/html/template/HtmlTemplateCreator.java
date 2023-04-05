package ru.itmo.ad.generator.html.template;

import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.HeaderTag;
import j2html.tags.specialized.HtmlTag;

import java.nio.file.Path;

import static j2html.TagCreator.*;

public class HtmlTemplateCreator {

    private final Path root;

    public HtmlTemplateCreator(Path root) {
        this.root = root;
    }

    public HtmlTag generate(
            String title,
            Path current,
            DomContent[] main
    ) {
        var pathToRoot = current.getParent().relativize(root);
        var stylesheetPath = pathToRoot.resolve("stylesheet.css");
        var overviewPath = pathToRoot.resolve("Overview.html");
        return TagCreator.html(
                TagCreator.head(
                        TagCreator.title(title),
                        link().withRel("stylesheet")
                                .withType("text/css")
                                .withHref(stylesheetPath.toString())
                                .withTitle("Style")),
                TagCreator.body(div(
                        overviewHref(overviewPath),
                        div(main(main)).withClass("flex-content")
                ).withClass("flex-box")).withClass("class-declaration-page"));
    }


    private static HeaderTag overviewHref(Path overview) {
        return TagCreator.header(nav(div(ul(
                        li(a("Overview").withHref(overview.toString()))
                ).withClass("nav-list")).withClass("top-nav")
        )).withClass("flex-header");
    }

}
