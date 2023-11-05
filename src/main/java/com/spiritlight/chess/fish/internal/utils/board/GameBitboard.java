package com.spiritlight.chess.fish.internal.utils.board;

import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.GameState;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.utils.StableField;
import com.spiritlight.chess.fish.internal.utils.arrays.IntArray;

import java.util.HashMap;
import java.util.Map;

import static com.spiritlight.chess.fish.game.Piece.*;

// bitboard sources:
// https://www.chessprogramming.org/PeSTO's_Evaluation_Function

public abstract class GameBitboard {
    private static final Map<Integer, GameBitboard> boardMap = new HashMap<>();
    private static final StableField<Boolean> initialized = new StableField<>(false);

    private static final GameBitboard EMPTY = new GameBitboard() {
        @Override protected IntArray early()  { return IntArray.createEmpty(64); }
        @Override protected IntArray middle() { return IntArray.createEmpty(64); }
        @Override protected IntArray end()    { return IntArray.createEmpty(64); }
    };

    public static void init() {
        if(initialized.get())  return;
        initialized.set(true);
        register(PAWN, new PawnBitboard());
        register(KNIGHT, new KnightBitboard());
        register(BISHOP, new BishopBitboard());
        register(ROOK, new RookBitboard());
        register(QUEEN, new QueenBitboard());
        register(KING, new KingBitboard());
    }

    /**
     * Retrieves the early game bitboard state.
     * @return
     */
    protected abstract IntArray early();

    /**
     * Retrieves the middle game bitboard state.
     * @return
     */
    protected abstract IntArray middle();

    /**
     * Retrieves the end game bitboard state.
     * @return
     */
    protected abstract IntArray end();

    public IntArray get(GameState state) {
        return switch (state) {
            case EARLY_GAME -> early();
            case MIDDLE_GAME -> middle();
            case END_GAME -> end();
            default -> throw new IllegalArgumentException("unexpected state: " + state);
        };
    }

    public static GameBitboard getBitboard(int piece) {
        return boardMap.getOrDefault(piece & ~Piece.COLOR_MASK, EMPTY);
    }

    public static IntArray getBitboard(int piece, GameState state) {
        return boardMap.getOrDefault(piece & ~COLOR_MASK, EMPTY).get(state);
    }

    public static void register(int piece, GameBitboard board) {
        register(piece, board, false);
    }

    public static void register(int piece, GameBitboard board, boolean override) {
        if(boardMap.containsKey(piece) && !override) throw new IllegalStateException("board with id " + piece + " already present. override with override parameter true");
        if(boardMap.containsValue(board)) InternLogger.getLogger().warn("Board with ID " + piece + " was registered with a duplicated board.");
        verifyBoard(board);
        boardMap.put(piece, board);
    }

    private static final RuntimeException WRONG_SIZE = new IllegalArgumentException("board has wrong size");
    private static void verifyBoard(GameBitboard board) {
        if(board.early().size() != 64) throw WRONG_SIZE;
        if(board.middle().size() != 64) throw WRONG_SIZE;
        if(board.end().size() != 64) throw WRONG_SIZE;
    }
}
