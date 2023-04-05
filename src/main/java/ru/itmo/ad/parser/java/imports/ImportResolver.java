package ru.itmo.ad.parser.java.imports;

import ru.itmo.ad.DocumentationApi;

import java.nio.file.Path;
import java.util.Map;

public class ImportResolver {

    private final Map<String, DocumentationApi.FileWithPath> javaFiles;

    public ImportResolver(Map<String, DocumentationApi.FileWithPath> javaFiles) {
        this.javaFiles = javaFiles;
    }

    public Path resolve(DocumentationApi.FileWithPath source, String value) {
        int pos = value.indexOf('.');
        if (pos != -1) {
            value = value.substring(0, pos);
        }
        Import imp = source.javaFile().imports().get(value);
        if (imp == null) {
            if (value.equals(source.javaFile().classes().get(0).name())) {
                return source.destination();
            }
            return null;
        }
        if (imp.importType() == ImportType.DEFAULT) {
            return javaFiles.get(imp.name()).destination();
        }
        // TODO: support static import
        return null;
    }
}
