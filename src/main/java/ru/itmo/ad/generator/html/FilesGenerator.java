package ru.itmo.ad.generator.html;

import ru.itmo.ad.DocumentationApi;
import ru.itmo.ad.parser.java.imports.ImportResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FilesGenerator {
    public void generate(Path destinationRoot, List<DocumentationApi.FileWithPath> files) throws IOException {
        var importResolver = new ImportResolver(files.stream().collect(Collectors.toMap(
                it -> it.javaFile().packageName() + '.' + it.javaFile().name(),
                it -> it
        )));
        var fileGenerator = new FileGenerator(importResolver, destinationRoot);
        for (var file : files) {
            try {
                fileGenerator.generate(file);
            } catch (Exception exception) {
                System.out.println("Exception for " + file.destination());
                throw exception;
            }
        }
        var overviewGenerator = new OverviewGenerator(importResolver, destinationRoot);
        overviewGenerator.generate(files);
    }
}
