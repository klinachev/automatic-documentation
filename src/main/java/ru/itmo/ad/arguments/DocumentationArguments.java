package ru.itmo.ad.arguments;

import java.nio.file.Path;

public record DocumentationArguments(
        Path source,
        Path destination
) {
}
