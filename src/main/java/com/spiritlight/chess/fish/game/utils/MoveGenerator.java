package com.spiritlight.chess.fish.game.utils;

import com.spiritlight.chess.fish.game.utils.board.AttackTable;
import com.spiritlight.chess.fish.game.utils.board.BoardMap;
import com.spiritlight.chess.fish.game.utils.game.Move;
import com.spiritlight.chess.fish.internal.utils.Bits;
import com.spiritlight.fishutils.misc.arrays.ReferenceArray;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.spiritlight.chess.fish.game.Piece.PIECE_MASK;

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
        int[] possibleDestinations = Bits.bitList(AttackTable.getDirect(piece & PIECE_MASK, index));
        List<Move> moves = new LinkedList<>();
        for(int move : possibleDestinations) {
            if(bitboard.canMove(index, move)) moves.add(Move.of(index, move));
        }
        return moves;
    }

    public static MoveGenerator create(BoardMap map) {
        return new MoveGenerator(map);
    }
}
