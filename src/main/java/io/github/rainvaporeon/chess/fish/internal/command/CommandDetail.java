package io.github.rainvaporeon.chess.fish.internal.command;

import java.util.function.Consumer;

public record CommandDetail(String key, Consumer<String> action, String description) {
}
