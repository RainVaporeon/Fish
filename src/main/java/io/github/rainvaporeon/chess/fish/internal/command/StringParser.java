package io.github.rainvaporeon.chess.fish.internal.command;

import java.util.function.Consumer;

public class StringParser {
    public static StringParser parseString(String str) {
        return new StringParser(str);
    }

    private final String value;
    private final String[] substring;

    private StringParser(String str) {
        value = str;
        substring = str.split(" ");
    }

    public String name() {
        return substring[0];
    }

    public void accept(String key, Consumer<String> consumer) {
        if(name().equals(key)) consumer.accept(value.substring(value.indexOf(" ") + 1));
    }
}
