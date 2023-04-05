package ru.itmo.ad;

import ru.itmo.ad.arguments.ArgumentsResolver;

/**
 * Main class!
 */
public class Main {

    /**
     * Main method!
     */
    public static void main(String[] args) {
        var argumentsResolver = new ArgumentsResolver();
        var documentationApi = new DocumentationApi();
        var arguments = argumentsResolver.resolve(args);
        if (arguments == null) {
            return;
        }
        try {
            documentationApi.generate(arguments);
        } catch (Throwable throwable) {
            System.out.println("Unexpected exception happened");
            throwable.printStackTrace();
        }
    }
}
