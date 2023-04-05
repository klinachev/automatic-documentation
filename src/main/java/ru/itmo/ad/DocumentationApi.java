package ru.itmo.ad;

import ru.itmo.ad.arguments.DocumentationArguments;
import ru.itmo.ad.generator.css.StylesheetCreator;
import ru.itmo.ad.generator.html.FilesGenerator;
import ru.itmo.ad.parser.java.JavaFile;
import ru.itmo.ad.parser.java.JavaParser;
import ru.itmo.ad.parser.java.ParseException;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DocumentationApi {
    private final JavaParser parser = new JavaParser();

    private final FilesGenerator filesGenerator = new FilesGenerator();
    private final StylesheetCreator stylesheetCreator = new StylesheetCreator();

    public void generate(DocumentationArguments arguments) {
        Path source = arguments.source();
        List<FileWithPath> javaFileWithPaths;
        if (Files.isDirectory(source)) {
            try (Stream<Path> walk = Files.walk(source)) {
                javaFileWithPaths = walk.map(p -> parse(source, arguments.destination(), p))
                        .filter(Objects::nonNull).toList();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            javaFileWithPaths = Stream.of(parse(Path.of(""), arguments.destination(), source))
                    .filter(Objects::nonNull).toList();
        }
        try {
            stylesheetCreator.create(arguments.destination());
            filesGenerator.generate(arguments.destination(), javaFileWithPaths);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private FileWithPath parse(Path sourceRoot, Path destinationRoot, Path current) {
        if (Files.isDirectory(current)) {
            return null;
        }
        if (!current.getFileName().toString().endsWith(".java")) {
            return null;
        }
        try {
            String newFileName = current.getFileName().toString().replace(".java", ".html");
            Path resolve = current.getParent() == null ? Path.of(newFileName)
                    : sourceRoot.relativize(current.getParent().resolve(newFileName));

            Path resolve1 = destinationRoot.resolve(resolve);
            return new FileWithPath(
                    parser.parse(
                            new Scanner(Files.newBufferedReader(current)),
                            current.getFileName().toString()
                    ),
                    current,
                    resolve1
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public record FileWithPath(JavaFile javaFile, Path source, Path destination) {
    }
}
