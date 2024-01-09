package io.github.rainvaporeon.chess.fish.internal.game.eval;

import com.spiritlight.fishutils.misc.exec.ExecutionContext;
import com.spiritlight.fishutils.misc.exec.ExecutionNode;
import io.github.rainvaporeon.chess.fish.internal.collections.PriorityHolder;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public class EvaluationContext implements ExecutionContext<BoardEvaluator> {
    @Override
    public ExecutionNode<BoardEvaluator> poll() {
        return null;
    }

    @Override
    public Collection<BoardEvaluator> accumulator() {
        return new PriorityHolder<>(8, Comparator.comparingDouble(e -> e.score));
    }

    @Override
    public Function<Collection<BoardEvaluator>, BoardEvaluator> finalizer() {
        return null;
    }
}
