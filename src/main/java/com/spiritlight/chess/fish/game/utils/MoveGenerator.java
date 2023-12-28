package com.spiritlight.chess.fish.game.utils;

import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.board.AttackTable;
import com.spiritlight.chess.fish.game.utils.board.BoardHelper;
import com.spiritlight.chess.fish.game.utils.board.BoardMap;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.utils.Bits;
import com.spiritlight.fishutils.misc.arrays.ReferenceArray;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.spiritlight.chess.fish.game.Piece.*;

public class MoveGenerator {
    /**
     * The bitboard.
     */
    private final BoardMap bitboard;
    /**
     * The board cursor.
     * The byte range is denoted by
     * <pre>
     * 00000000 00000000 00000000 00000000
     *      UNUSED      |  PIECE | INDEX
     * </pre>
     * with the piece following the standard
     * number denoted in {@link com.spiritlight.chess.fish.game.Piece},
     * color excluded.
     */
    private int cursor;

    private MoveGenerator(BoardMap board) {
        this.bitboard = board;
        this.cursor = 0;
    }

    public List<Move> getAllValidMoves() {
        List<Move> totalMoves = new LinkedList<>();
        for(int i = 0; i < 64; i++) {
            totalMoves.addAll(this.getValidMovesFor(i));
        }
        return totalMoves;
    }

    public List<Move> getValidMovesFor(int index) {
        int piece = bitboard.getPieceAt(index);
        if(Piece.is(piece, PAWN)) return processPawn(index);
        int[] possibleDestinations = Bits.bitList(AttackTable.getDirect(piece & PIECE_MASK, index));
        List<Move> moves = new LinkedList<>();
        for(int move : possibleDestinations) {
            if(bitboard.canMove(index, move)) moves.add(Move.of(index, move));
        }
        return moves;
    }

    private List<Move> processPawn(int index) {
        List<Move> moves = new LinkedList<>();
        final int[] possibleMoves = {1, -1, 8, -8, 9, -9, 7, -7};
        for(int move : possibleMoves) {
            if(bitboard.canMove(index, index + move)) moves.add(Move.of(index, index + move));
        }
        if(moves.size() > 3) InternLogger.getLogger().warn(STR."P@\{Move.parseLocation(index)} has more than 3 moves: \{moves.toString()}");
        return moves;
    }

    public static MoveGenerator create(BoardMap map) {
        return new MoveGenerator(map);
    }
}
