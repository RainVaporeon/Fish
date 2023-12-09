package com.spiritlight.chess.fish.internal.utils.boot;

import com.spiritlight.fishutils.collections.Pair;

import java.util.function.Consumer;

@FunctionalInterface
public interface LaunchOption {

    /**
     * Retrieves the option pair.
     * The consumer supplied will be taking the parameters
     * from the option.
     * @return the pair
     * @apiNote Note that the consumer may have null passed into
     * the parameter, indicating the flag was not set.
     */
    Pair<String, Consumer<String>> getActionPair();

    static String getString(LaunchOption option) {
        return STR."\{option.getActionPair().getKey()}:\{option.getActionPair()}";
    }
}
