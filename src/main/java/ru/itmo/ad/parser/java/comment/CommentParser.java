package ru.itmo.ad.parser.java.comment;

import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.List;

public class CommentParser {

    // example
    /*example 2 */

    /**
     * example 3
     */
    public List<String> parse(Scanner sc) {
        if (sc.takeString("//")) {
            int pos = sc.loadUntilNextLine();
            String substring = sc.getString().substring(0, pos);
            sc.dropUntil(pos);
            return List.of(substring);
        }
        if (sc.takeString("/*")) {
            sc.takeString("*");
            sc.dropWhitespaces();
            int i = 0;
            sc.loadOrThrow(i);
            char ch = sc.charAt(i);
            if (ch == '*') {
                sc.loadOrThrow(i + 2);
                if (sc.charAt(i + 1) != '/') {
                    ch = sc.charAt(++i);
                }
            }
            var sb = new StringBuilder();
            for (; ; ++i) {
                sc.loadOrThrow(i + 1);
                if (ch == '*' && sc.charAt(i) == '/') {
                    sc.dropUntil(i + 1);
                    return List.of(sb.toString());
                }
                sb.append(ch);
                ch = sc.charAt(i);
                while (ch == '\n' || ch == '\r') {
                    while (Character.isWhitespace(ch)) {
                        sc.loadOrThrow(++i + 1);
                        ch = sc.charAt(i);
                    }
                    if (ch == '*') {
                        sc.loadOrThrow(i + 2);
                        if (sc.charAt(i + 1) != '/') {
                            ch = sc.charAt(++i);
                        }
                    }
                    while (Character.isWhitespace(ch)) {
                        sc.loadOrThrow(i + 1);
                        ch = sc.charAt(++i);
                    }
                    sb.append(' ');
                }
            }
        }
        return List.of();
    }
}
