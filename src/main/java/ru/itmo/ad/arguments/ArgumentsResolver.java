package ru.itmo.ad.arguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArgumentsResolver {
    public DocumentationArguments resolve(String[] args) {
        if (args.length < 2) {
            System.out.println("2 arguments expected");
            return null;
        }
        Path source = getPath(args[0], false);
        Path destination = getPath(args[1], true);
        if (destination == null || source == null) {
            return null;
        }
        return new DocumentationArguments(source, destination);
    }

    private static boolean createDir(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            System.out.println("failed to create: " + path);
            return false;
        }
        System.out.println("Created dir: " + path);
        return true;
    }

    private static Path getPath(String args, boolean create) {
        Path path = Path.of(args);
        if (!Files.exists(path)) {
            if (create) {
                if (createDir(path)) {
                    return path;
                }
                return null;
            }
            System.out.println("Provided path is invalid: " + path);
            return null;
        }
        return path;
    }
}
