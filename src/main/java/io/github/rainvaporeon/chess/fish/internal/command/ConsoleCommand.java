package io.github.rainvaporeon.chess.fish.internal.command;

import com.spiritlight.fishutils.misc.StableField;
import com.spiritlight.fishutils.misc.effects.Effect;
import io.github.rainvaporeon.chess.fish.internal.utils.SharedConstants;

import java.util.HashSet;
import java.util.Set;

public class ConsoleCommand {

    private static final StableField<Boolean> inited = new StableField<>(false);
    private static final Effect<String> command = Effect.ofConcurrent("");
    private static final Set<CommandDetail> details = new HashSet<>();

    public static void registerDetail(CommandDetail detail) {
        details.add(detail);
    }

    public static void init() {

        if (inited.get()) return;
        inited.set(true);
        Thread.ofVirtual().name("ConsoleCommand/IO").start(() -> {
            while (true) {
                System.out.print("ConsoleCommand #>");
                command.set(SharedConstants.IN.nextLine());
                System.out.println();
            }
        });

        registerDetail(new CommandDetail("help", _ -> {
            StringBuilder builder = new StringBuilder("Fish - Help\n");
            for(CommandDetail detail : details) {
                builder.append(STR."\{detail.key()} \{detail.description()}\n");
            }
            System.out.println(builder);
        }, "Shows help messages"));

        command.addListener(ConsoleCommand::parse);
    }

    private static void parse(String command) {
        StringParser parser = StringParser.parseString(command);
        for(CommandDetail detail : details) {
            if(detail.key().equals(parser.name())) parser.accept(detail.key(), detail.action());
        }
    }
}
