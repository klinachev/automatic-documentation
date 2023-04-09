package ru.itmo.ad.parser.java.comment;

import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

public class CommentParser {

    public List<String> parseAll(Scanner sc) {
        var list = new ArrayList<String>();
        var comment = parse(sc);
        while (!comment.isEmpty()) {
            list.addAll(comment);
            comment = parse(sc);
        }
        return list;
    }

    // comment example
    /* comment example 2 */
    /**
     * comment example 3
     */
    public List<String> parse(Scanner sc) {
        if (sc.takeString("//")) {
            int pos = sc.loadUntilNextLine();
            String substring = sc.getString().substring(0, pos);
            sc.dropUntil(pos);
            return List.of(substring);
        }
        var list = new ArrayList<String>();
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
            ch = sc.charAt(i++);
            for (; ; ++i) {
                sc.loadOrThrow(i + 1);
                if (ch == '*' && sc.charAt(i) == '/') {
                    sc.dropUntil(i + 1);
                    list.add(sb.toString());
                    return list;
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
                    while (Character.isWhitespace(ch) && !(ch == '\n' || ch == '\r')) {
                        sc.loadOrThrow(i + 2);
                        ch = sc.charAt(++i);
                    }
                    if (ch == '\n' || ch == '\r') {
                        sb.append("<p>");
                    } else {
                        sb.append(' ');
                    }
                    list.add(sb.toString());
                    sb = new StringBuilder();
                    sc.dropUntil(i);
                    i = 0;
                }
            }
        }
        return List.of();
    }
}
