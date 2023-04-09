package ru.itmo.ad.parser.java.utils;

import ru.itmo.ad.parser.java.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;

public class Scanner {
    private final BufferedReader bufferedReader;
    private final char[] chars = new char[2048];

    private String string = "";

    public Scanner(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public Scanner(String s) {
        this(new BufferedReader(new StringReader(s)));
    }

    public int load() {
        try {
            int read = bufferedReader.read(chars);
            string = string + new String(chars);
            return read;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int loadOrThrow(int minSize) {
        int pos = load(minSize);
        if (pos == -1) {
            throw new ParseException("Unexpected end");
        }
        return pos;
    }

    public int load(int minSize) {
        if (minSize < string.length()) {
            return 0;
        }
        int read = 0;
        try {
            var sb = new StringBuilder();
            while (string.length() + read < minSize) {
                int read1 = bufferedReader.read(chars);
                read += read1;
                sb.append(new String(chars));
                if (read1 == -1) {
                    return -1;
                }
            }
            string += sb.toString();
            return read;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int loadUntilSpace() {
        return loadUntilSpaceOrChars(Set.of(' '));
    }

    public int loadUntilSpaceOrChar(char ch) {
        return loadUntilSpaceOrChars(Set.of(ch));
    }

    public int loadUntilSpaceOrChars(Collection<Character> characters) {
        loadWhileWhitespaces();
        return loadUntilSpaceOrChar(0, characters);
    }

    private int loadUntilSpaceOrChar(int start, Collection<Character> characters) {
        for (int i = start; i < string.length(); ++i) {
            if (Character.isWhitespace(string.charAt(i)) || characters.contains(string.charAt(i))) {
                return i;
            }
        }
        int load = load();
        if (load == -1) {
            return -1;
        }
        return loadUntilSpaceOrChar(start, characters);
    }

    public int loadWhileWhitespaces() {
        return loadWhileWhitespaces(0);
    }

    private int loadWhileWhitespaces(int start) {
        for (int i = start; i < string.length(); ++i) {
            if (!Character.isWhitespace(string.charAt(i))) {
                string = string.substring(i);
                return i;
            }
        }
        int load = load();
        if (load == -1) {
            return -1;
        }
        return loadWhileWhitespaces(start);
    }

    public int loadUntil(char ch) {
        return loadUntil(0, Set.of(ch));
    }

    public int loadUntilNextLine() {
        return loadUntil(0, Set.of('\n', '\r'));
    }

    public int loadUntilAnyChar(Collection<Character> characters) {
        return loadUntil(0, characters);
    }

    private int loadUntil(int start, Collection<Character> characters) {
        for (int i = start; i < string.length(); ++i) {
            if (characters.contains(string.charAt(i))) {
                return i;
            }
        }
        int load = load();
        if (load == -1) {
            return -1;
        }
        return loadUntil(start, characters);
    }

    public void dropWhitespaces() {
        int i = 0;
        load(1);
        while (Character.isWhitespace(string.charAt(i))) {
            i++;
            load(i + 1);
        }
        string = string.substring(i);
    }

    public void dropUntil(int pos) {
        string = string.substring(pos);
    }

    public String getString() {
        return string;
    }


    public char charAt(int i) {
        return string.charAt(i);
    }

    public String takeString() {
        int pos = loadUntilSpaceOrChar(' ');
        if (pos == -1) {
            return null;
        }
        var result = string.substring(0, pos);
        dropUntil(pos);
        return result;
    }

    public String takeStringOrThrow() {
        var string = takeString();
        if (string == null) {
            throw new ParseException("Unexpected end");
        }
        return string;
    }

    public String takeStringUntil(Collection<Character> characters) {
        int pos = loadUntilSpaceOrChars(characters);
        if (pos == -1) {
            return null;
        }
        var result = string.substring(pos);
        dropUntil(pos);
        return result;
    }

    public boolean takeWord(String s) {
        loadWhileWhitespaces();
        load(s.length() + 1);
        boolean found = string.startsWith(s);
        if (Character.isAlphabetic(string.charAt(s.length()))) {
            return false;
        }
        if (found) {
            dropUntil(s.length());
        }
        return found;
    }

    public boolean takeString(String s) {
        loadWhileWhitespaces();
        load(s.length());
        boolean found = string.startsWith(s);
        if (found) {
            dropUntil(s.length());
        }
        return found;
    }

    public void takeStringOrThrow(String s) {
        var take = takeString(s);
        if (!take) {
            throw new ParseException(s + " expected at: " + string);
        }
    }
}
