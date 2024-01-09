package io.github.rainvaporeon.chess.fish.internal.game.eval;

import com.spiritlight.fishutils.misc.exec.ExecutionNode;

import java.util.concurrent.Future;

public class EvaluationNode extends ExecutionNode<BoardEvaluator> {
    private final EvaluationNode parent;
    private final BoardEvaluator evaluator;
    private final int move;

    EvaluationNode(BoardEvaluator base) {
        this.parent = null;
        this.evaluator = base;
        this.move = -1;
    }

    private EvaluationNode(EvaluationNode parent, BoardEvaluator base, int path) {
        this.parent = parent;
        this.evaluator = base;
        this.move = path;
    }

    @Override
    public Future<BoardEvaluator> get() {
        return null;
    }

    @Override
    protected ExecutionNode<BoardEvaluator> parent() {
        return parent;
    }

    @Override
    public void run() {

    }
}
