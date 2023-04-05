package ru.itmo.ad.generator.css;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class StylesheetCreator {
    public void create(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            path = path.toAbsolutePath().getParent();
        }
        path = path.resolve("stylesheet.css");
        Path source;
        try {
            URL resource = getClass().getResource("/stylesheet.css");
            if (resource == null) {
                throw new IOException("stylesheet.css not found");
            }
            source = Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        Files.copy(source, path, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("stylesheet.css generated.");
    }
}
