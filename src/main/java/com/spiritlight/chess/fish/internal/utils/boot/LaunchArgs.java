package com.spiritlight.chess.fish.internal.utils.boot;

import com.spiritlight.chess.fish.internal.exceptions.SystemError;
import com.spiritlight.fishutils.collections.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LaunchArgs {
    private static final Map<String, Consumer<String>> optionMap = new HashMap<>() {{
        put(null, s -> {});
    }};
    private static final Map<String, String> argMap = new HashMap<>();

    /**
     * Initializes the launch args. This should only be executed once.
     * Upon initialization, all underlying registers are removed from the
     * option map.
     */
    public static void init() {
        optionMap.forEach((key, handler) -> {
            if(key == null) return;
            String params = System.getProperty(key);
            handler.accept(params);
            argMap.put(key, params);
        });
        optionMap.clear();
    }

    public static String getOption(String option) {
        return argMap.get(option);
    }

    public static boolean isKeyPresent(String option) {
        return argMap.containsKey(option);
    }

    /**
     * Registers this launch
     * @param option the option
     */
    public static void register(LaunchOption option) {
        if(optionMap.isEmpty()) throw new SystemError("late launch option: " + LaunchOption.getString(option));
        Pair<String, Consumer<String>> pair = option.getActionPair();
        optionMap.put(pair.getKey(), pair.getValue());
    }
}
