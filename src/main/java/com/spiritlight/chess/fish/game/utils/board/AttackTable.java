package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

import static com.spiritlight.chess.fish.game.Piece.*;

public class AttackTable {
    private static final LongArray knight;
    private static final LongArray bishop;
    private static final LongArray rook;
    private static final LongArray queen;
    private static final LongArray king;

    private static final long[] directKnight, directBishop, directRook, directQueen, directKing;

    static {
        long[] R = new long[64];
        for(int i = 0; i < 64; i++) {
            R[i] = setAll(Piece.sliding(Piece.ROOK, i));
        }

        long[] B = new long[64];
        for(int i = 0; i < 64; i++) {
            B[i] = setAll(Piece.sliding(Piece.BISHOP, i));
        }

        long[] N = new long[64];
        for(int i = 0; i < 64; i++) {
            N[i] = setAll(Piece.getKnightAttackSquares(i));
        }

        final int[] kingOffsets = {1, -1, 7, -7, 9, -9, 8, -8};
        long[] K = new long[64];
        for(int i = 0; i < 64; i++) {
            long value = 0;
            for(int offset : kingOffsets) {
                if(i + offset > 0 && i + offset < 64) {
                    long mask = 1L << (i + offset);
                    value |= mask;
                }
            }
            K[i] = value;
        }

        knight = LongArray.fromArray(N);
        directKnight = N;
        bishop = LongArray.fromArray(B);
        directBishop = B;
        rook = LongArray.fromArray(R);
        directRook = R;
        queen = LongArray.create(64, i -> R[i] | B[i]);
        directQueen = queen.toLongArray();
        king = LongArray.fromArray(K);
        directKing = K;
    }

    private static final LongArray EMPTY = new LongArray(new long[64]);

    /**
     * Gets the attack table array of a given piece type
     * @param piece the piece
     * @return an immutable long array describing all reachable
     * locations, in order of position. Empty if it is a pawn.
     */
    public static LongArray get(int piece) {
        return switch (piece) {
            case QUEEN -> queen;
            case ROOK -> rook;
            case BISHOP -> bishop;
            case KNIGHT -> knight;
            case KING -> king;
            // King is trivial, pawns are handled differently
            default -> EMPTY;
        };
    }

    /**
     * Directly acquire the attack table of a given piece type
     * at the location.
     * @param piece the piece
     * @param position the position, from 0 to 63
     * @return a 64-bit integer describing the attack range, zero if
     * a pawn was supplied.
     * @apiNote it is generally faster and more direct to use this
     * method in comparison to {@link AttackTable#get(int)}
     */
    public static long getDirect(int piece, int position) {
        return switch (piece) {
            case QUEEN -> directQueen[position];
            case ROOK -> directRook[position];
            case BISHOP -> directBishop[position];
            case KNIGHT -> directKnight[position];
            case KING -> directKing[position];
            default -> 0;
        };
    }

    public static long getMaskAt(int piece, int src) {
        return AttackTable.getDirect(piece, src);
    }

    public static LongArray getKnight() {
        return knight;
    }

    public static LongArray getBishop() {
        return bishop;
    }

    public static LongArray getRook() {
        return rook;
    }

    public static LongArray getQueen() {
        return queen;
    }

    public static LongArray getKing() {
        return king;
    }

    public static void init() {}

    private static long setAll(int[] place) {
        long l = 0;
        for(int bit : place) {
            l |= mask(bit);
        }
        return l;
    }

    private static long mask(int location) {
        return 1L << location;
    }
}
