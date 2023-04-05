package ru.itmo.ad.parser.java.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.ad.parser.java.utils.Scanner;

import java.util.ArrayList;
import java.util.List;

class CommentParserTest {

    private final CommentParser commentParser = new CommentParser();


    @Test
    void testNoComments() {
        var target = "class A {}";
        var comments = commentParser.parse(new Scanner(target));
        Assertions.assertTrue(comments.isEmpty());
    }

    @Test
    void testSimpleComments() {
        var target = """
                // a   b    c
                /* 1  2  3 */
                /** z  x  c */
                class A {}
                """;
        var comments = parse(target);
        Assertions.assertEquals(
                List.of(" a   b    c",
                        "1  2  3 ",
                        "z  x  c "
                ),
                comments);
    }

    @Test
    void testMultipleLineComments() {
        var target = """
                    // a   b    c
                    // a   b    c2
                    /* 1  2 
                    
                     3 
                     */
                    /** z  x
                     * 1 2
                     * 1 2
                     * c
                    */
                class A {}
                """;
        var comments = parse(target);
        Assertions.assertEquals(
                List.of(" a   b    c",
                        " a   b    c2",
                        "1  2 3 ",
                        "z  x 1 2 1 2 c "
                ),
                comments);
    }

    private ArrayList<String> parse(String target) {
        Scanner sc = new Scanner(target);
        var comments = new ArrayList<String>();
        var comment = commentParser.parse(sc);
        while (!comment.isEmpty()) {
            comments.addAll(comment);
            comment = commentParser.parse(sc);
        }
        return comments;
    }

}