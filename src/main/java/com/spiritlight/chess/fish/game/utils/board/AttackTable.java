package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

public class AttackTable {
    private static final LongArray knight;
    private static final LongArray bishop;
    private static final LongArray rook;
    private static final LongArray queen;

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

        knight = LongArray.fromArray(N);
        bishop = LongArray.fromArray(B);
        rook = LongArray.fromArray(R);
        queen = LongArray.create(64, i -> R[i] | B[i]);
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
