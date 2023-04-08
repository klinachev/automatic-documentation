package ru.itmo.ad.generator.html;

import ru.itmo.ad.FileWithPath;
import ru.itmo.ad.calculation.ImportResolver;
import ru.itmo.ad.calculation.TerminalStatesResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FilesGenerator {
    public void generate(Path destinationRoot, List<FileWithPath> files) throws IOException {
        var importResolver = new ImportResolver(files.stream().collect(Collectors.toMap(
                it -> it.javaFile().packageName() + '.' + it.javaFile().name(),
                it -> it
        )));
        var terminalStatesResolver = new TerminalStatesResolver(importResolver);
        terminalStatesResolver.calculate(files);
        var fileGenerator = new FileGenerator(importResolver, destinationRoot);
        for (var file : files) {
            try {
                fileGenerator.generate(file);
            } catch (IOException exception) {
                System.out.println("Exception for " + file.destination());
                throw exception;
            }
        }
        var overviewGenerator = new OverviewGenerator(importResolver, destinationRoot);
        overviewGenerator.generate(files);
    }
}
