package com.spiritlight.chess.fish.game.utils.game;

import com.spiritlight.chess.fish.game.utils.GameState;
import com.spiritlight.chess.fish.game.utils.board.BoardMap;
import com.spiritlight.chess.fish.internal.utils.board.GameBitboard;
import com.spiritlight.fishutils.misc.arrays.primitive.IntArray;

import static com.spiritlight.chess.fish.game.Piece.*;

public class BoardEvaluator {

    private static final int PAWN_SCORE = 100;
    private static final int KNIGHT_SCORE = 300;
    private static final int BISHOP_SCORE = 330;
    private static final int ROOK_SCORE = 500;
    private static final int QUEEN_SCORE = 900;

    public static String evaluateFormatted(BoardMap map, GameState state) {
        return String.format("%.2f", evaluate(map, state));
    }

    public static double evaluate(BoardMap map, GameState state) {
        if(map.getColor() != WHITE) map = map.getEnemyBoard();
        return getPositionBonus(map, state) + getMaterialScore(map);
    }

    // TODO: There has to be a better approach, this is simply too slow.
    private static double getPositionBonus(BoardMap map, GameState state) {
        BoardMap.BoardItr itr = map.itr();
        BoardMap.BoardItr enemyItr = map.getEnemyBoard().itr();
        double value = 0;
        while (itr.hasNext() && enemyItr.hasNext()) {
            long layout = itr.next();
            long enemyLayout = itr.next();
            IntArray bonus = GameBitboard.getBitboard(itr.cursorPiece(), state);
            IntArray enemyBonus = GameBitboard.getBitboard(BLACK | itr.cursorPiece(), state);
            for(int i = 0; i < 64; i++) {
                value += bonus.get(i) * getBitAt(i, layout);
                value -= enemyBonus.get(i) * getBitAt(i, enemyLayout);
            }
        }
        return value;
    }

    private static int getBitAt(int idx, long value) {
        long mask = 1L << idx;
        return (value & mask) == 0 ? 0 : 1;
    }

    private static int getMaterialScore(BoardMap map) {
        BoardMap.BoardItr itr = map.itr();
        BoardMap.BoardItr enemyItr = map.getEnemyBoard().itr();
        int value = 0;
        while(itr.hasNext() && enemyItr.hasNext()) {
            long next = itr.next();
            long enemyNext = enemyItr.next();
            if(next == 0) continue;
            int count = Long.bitCount(next);
            int enemyCount = Long.bitCount(enemyNext);
            value += (count - enemyCount) * getPieceScore(itr.cursorPiece());
        }
        return value;
    }

    private static int getPieceScore(int piece) {
        return switch (piece & ~COLOR_MASK) {
            case PAWN -> PAWN_SCORE;
            case KNIGHT -> KNIGHT_SCORE;
            case BISHOP -> BISHOP_SCORE;
            case ROOK -> ROOK_SCORE;
            case QUEEN -> QUEEN_SCORE;
            case KING -> Integer.MAX_VALUE >> 1;
            default -> throw new IllegalStateException(STR."Unexpected value: \{piece & ~COLOR_MASK}");
        };
    }

    private static double deferBitboardValue(int piece, long position) {
        return 0;
    }

}
