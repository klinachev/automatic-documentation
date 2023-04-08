package ru.itmo.ad;


import ru.itmo.ad.parser.java.JavaFile;

import java.nio.file.Path;

public record FileWithPath(JavaFile javaFile, Path source, Path destination) {
}