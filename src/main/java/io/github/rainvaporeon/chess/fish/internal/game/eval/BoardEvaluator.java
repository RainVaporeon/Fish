package io.github.rainvaporeon.chess.fish.internal.game.eval;

import io.github.rainvaporeon.chess.fish.game.utils.GameState;
import io.github.rainvaporeon.chess.fish.game.utils.board.BoardMap;
import io.github.rainvaporeon.chess.fish.internal.utils.board.GameBitboard;
import com.spiritlight.fishutils.misc.arrays.primitive.IntArray;

import java.net.URL;
import java.util.Iterator;

import static io.github.rainvaporeon.chess.fish.game.Piece.*;

/**
 * Utility class to evaluate a board's scoring.
 * The higher the score, the more advantageous white has.
 * This should be used alongside other implementations and should
 * be properly synchronized as it is relatively resource demanding.
 * @apiNote This class should be used with a move generator with the
 *          pre-generated move outcome into this evaluator for
 *          evaluation and other purposes.
 */
public class BoardEvaluator {
    private static final int PAWN_SCORE = 100;
    private static final int KNIGHT_SCORE = 300;
    private static final int BISHOP_SCORE = 350;
    private static final int ROOK_SCORE = 500;
    private static final int QUEEN_SCORE = 900;

    // We keep a copy of this and evaluate from here instead
    private final long king;
    private final long queen;
    private final long rook;
    private final long bishop;
    private final long knight;
    private final long pawn;

    private final long blackKing;
    private final long blackQueen;
    private final long blackRook;
    private final long blackBishop;
    private final long blackKnight;
    private final long blackPawn;

    private int halfMove; // tracked in case of 50-move
    private final int[] moveTraceback; // used to trace the moves required to get back/detecting repetition,
                                       // also as a node return

    double score;

    private final GameState state;

    public BoardEvaluator(BoardMap map, int... moveTraceback) {
        BoardMap.BoardItr white = map.itr();
        BoardMap.BoardItr black = map.getEnemyBoard().itr();
        pawn = white.nextLong();
        knight = white.nextLong();
        bishop = white.nextLong();
        rook = white.nextLong();
        queen = white.nextLong();
        king = white.nextLong();

        blackPawn = black.nextLong();
        blackKnight = black.nextLong();
        blackBishop = black.nextLong();
        blackRook = black.nextLong();
        blackQueen = black.nextLong();
        blackKing = black.nextLong();

        state = map.getState();

        this.moveTraceback = moveTraceback;
    }

    private BoardEvaluator(long king, long queen, long rook, long bishop, long knight, long pawn, long blackKing, long blackQueen, long blackRook, long blackBishop, long blackKnight, long blackPawn, int halfMove, int[] moveTraceback, GameState state) {
        this.king = king;
        this.queen = queen;
        this.rook = rook;
        this.bishop = bishop;
        this.knight = knight;
        this.pawn = pawn;
        this.blackKing = blackKing;
        this.blackQueen = blackQueen;
        this.blackRook = blackRook;
        this.blackBishop = blackBishop;
        this.blackKnight = blackKnight;
        this.blackPawn = blackPawn;
        this.halfMove = halfMove;
        this.moveTraceback = moveTraceback;
        this.state = state;
    }

    public String evaluateFormatted() {
        return String.format("%.2f", evaluate());
    }

    public double evaluate() {
        if(checkDrawCondition()) return 0;
        return getPositionBonus() + getMaterialScore();
    }

    private boolean checkDrawCondition() {
        return this.halfMove == 100; // stalemate checked separately
    }

    // TODO: There has to be a better approach, this is simply too slow.
    private double getPositionBonus() {
        double value = 0;
        Itr itr = new Itr();
        while (itr.hasNext()) {
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

    private int getMaterialScore() {
        Itr itr = new Itr();
        int value = 0;
        while(itr.hasNext()) {
            long next = itr.next();
            long enemyNext = itr.next();
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
            case KING -> Integer.MAX_VALUE >> 4;
            default -> throw new IllegalStateException(STR."Unexpected value: \{piece & ~COLOR_MASK}");
        };
    }

    private class Itr implements Iterator<Long> {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < 12;
        }

        public int cursorPiece() {
            return switch (cursor / 2) {
                case 0 -> PAWN;
                case 1 -> KNIGHT;
                case 2 -> BISHOP;
                case 3 -> ROOK;
                case 4 -> QUEEN;
                case 5, 6 -> KING;
                default -> throw new IllegalStateException(STR."Unexpected cursor value: \{cursor}");
            };
        }

        public long nextLong() {
            return switch (cursor++) {
                case 0 -> pawn;
                case 1 -> blackPawn;
                case 2 -> knight;
                case 3 -> blackKnight;
                case 4 -> bishop;
                case 5 -> blackBishop;
                case 6 -> rook;
                case 7 -> blackRook;
                case 8 -> queen;
                case 9 -> blackQueen;
                case 10 -> king;
                case 11 -> blackKing;
                default -> throw new IllegalStateException(STR."Unexpected value: \{cursor}");
            };
        }

        @Override
        public Long next() {
            return nextLong();
        }
    }
}
