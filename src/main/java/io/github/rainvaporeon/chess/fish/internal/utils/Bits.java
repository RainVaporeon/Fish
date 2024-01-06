package io.github.rainvaporeon.chess.fish.internal.utils;

import io.github.rainvaporeon.chess.fish.game.Piece;
import io.github.rainvaporeon.chess.fish.game.utils.board.AttackTable;
import io.github.rainvaporeon.chess.fish.game.utils.board.Magic;
import com.spiritlight.fishutils.collections.IntList;
import io.github.rainvaporeon.chess.fish.game.utils.game.Move;
import io.github.rainvaporeon.chess.fish.internal.jnative.NativeMagicBoard;

import java.util.ArrayList;
import java.util.List;

import static io.github.rainvaporeon.chess.fish.game.Piece.*;

public class Bits {
    private static final double LOG_2 = Math.log(2);
    private static final long[] ROOK_RAY = new long[64];
    private static final long[] BISHOP_RAY = new long[64];
    private static final long[] QUEEN_RAY = new long[64];

    static {
        for(int i = 0; i < 64; i++) {
            long N = AttackTable.getRay(i, AttackTable.NORTH);
            long E = AttackTable.getRay(i, AttackTable.EAST);
            long S = AttackTable.getRay(i, AttackTable.SOUTH);
            long W = AttackTable.getRay(i, AttackTable.WEST);
            long NE = AttackTable.getRay(i, AttackTable.NORTHEAST);
            long NW = AttackTable.getRay(i, AttackTable.NORTHWEST);
            long SE = AttackTable.getRay(i, AttackTable.SOUTHEAST);
            long SW = AttackTable.getRay(i, AttackTable.SOUTHWEST);
            ROOK_RAY[i] = N | E | S | W;
            BISHOP_RAY[i] = NE | NW | SE | SW;
            QUEEN_RAY[i] = N | E | S | W | NE | NW | SE | SW;
        }
    }

    private enum Type {
        NATIVE, SELF
    }

    private static final Type type = Type.NATIVE;

    public static int[] bitList(long value) {
        IntList list = new IntList(8);
        while(value != 0) {
            long mask = Long.highestOneBit(value);
            int position = (int) (Math.log(mask) / LOG_2) - 1; // return actual position
            value ^= mask; // turn that bit off
            list.add(position); // record the position
        }
        return list.toIntArray();
    }

    public static List<Move> convertBoardToMove(int src, long mask) {
        List<Move> list = new ArrayList<>(Long.bitCount(mask));
        for(int i = 0; i < 64; i++) {
            long l = i == 0 ? 0 : 1L << (i - 1);
            if((mask & l) != 0) list.add(Move.of(src, i));
        }
        return list;
    }

    public static long getRayAttack(long blocking, int pos, int piece) {
        if(Piece.is(piece, ROOK)) return getRookRay(blocking, pos);
        if(Piece.is(piece, Piece.BISHOP)) return getBishopRay(blocking, pos);
        if(Piece.is(piece, Piece.QUEEN)) return getQueenRay(blocking, pos);
        return 0;
    }

    public static long getRayAttack(int pos, int piece) {
        if(Piece.is(piece, ROOK)) return ROOK_RAY[pos];
        if(Piece.is(piece, BISHOP)) return BISHOP_RAY[pos];
        if(Piece.is(piece, QUEEN)) return QUEEN_RAY[pos];
        return 0;
    }

    public static long getRayAttackMagic(long blocking, int pos, int piece) {
        if(Piece.is(piece, ROOK)) return getRookRayMagic(blocking, pos);
        if(Piece.is(piece, Piece.BISHOP)) return getBishopRayMagic(blocking, pos);
        if(Piece.is(piece, Piece.QUEEN)) return getQueenRayMagic(blocking, pos);
        return 0;
    }

    private static long getRookRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return NativeMagicBoard.getRook(blocking, pos);
        long attack = AttackTable.getDirect(ROOK, pos);
        long magic = Magic.get(ROOK, pos);
        int shiftCount = 64 - Long.bitCount(magic);
        return ((attack & blocking) * magic) >>> shiftCount;
    }

    private static long getBishopRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return NativeMagicBoard.getBishop(blocking, pos);
        long attack = AttackTable.getDirect(BISHOP, pos);
        long magic = Magic.get(BISHOP, pos);
        int shiftCount = 64 - Long.bitCount(magic);
        return ((attack & blocking) * magic) >>> shiftCount;
    }

    private static long getQueenRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return NativeMagicBoard.getQueen(blocking, pos);
        return getRookRayMagic(blocking, pos) | getBishopRayMagic(blocking, pos);
    }

    public static long getRookRay(long blocking, int pos) {
        long N = AttackTable.getRay(pos, AttackTable.NORTH);
        long E = AttackTable.getRay(pos, AttackTable.EAST);
        long S = AttackTable.getRay(pos, AttackTable.SOUTH);
        long W = AttackTable.getRay(pos, AttackTable.WEST);

        long filteredMaskN = AttackTable.getRay(ls1bPos(N & blocking), AttackTable.NORTH);
        long filteredMaskE = AttackTable.getRay(ls1bPos(E & blocking), AttackTable.EAST);
        long filteredMaskS = AttackTable.getRay(ms1bPos(S & blocking), AttackTable.SOUTH);
        long filteredMaskW = AttackTable.getRay(ms1bPos(W & blocking), AttackTable.WEST);

        return (N ^ filteredMaskN) | (S ^ filteredMaskS) | (E ^ filteredMaskE) | (W | filteredMaskW);
    }

    public static long getBishopRay(long blocking, int pos) {
        long NE = AttackTable.getRay(pos, AttackTable.NORTHEAST);
        long NW = AttackTable.getRay(pos, AttackTable.NORTHWEST);
        long SE = AttackTable.getRay(pos, AttackTable.SOUTHEAST);
        long SW = AttackTable.getRay(pos, AttackTable.SOUTHWEST);

        long filteredMaskNE = AttackTable.getRay(ls1bPos(NE & blocking), AttackTable.NORTHEAST);
        long filteredMaskNW = AttackTable.getRay(ls1bPos(NW & blocking), AttackTable.NORTHWEST);
        long filteredMaskSE = AttackTable.getRay(ms1bPos(SE & blocking), AttackTable.SOUTHEAST);
        long filteredMaskSW = AttackTable.getRay(ms1bPos(SW & blocking), AttackTable.SOUTHWEST);

        return (NE ^ filteredMaskNE) | (NW ^ filteredMaskNW) | (SE ^ filteredMaskSE) | (SW | filteredMaskSW);
    }

    public static long getQueenRay(long blocking, int pos) {
        return getBishopRay(blocking, pos) | getRookRay(blocking, pos);
    }

    // Forward bitscan (for positive offsets)
    private static long ls1b(long value) {
        return Long.lowestOneBit(value);
    }

    private static int ls1bPos(long value) {
        return Long.numberOfTrailingZeros(value);
    }

    // Reverse bitscan (for negative offsets)
    private static long ms1b(long value) {
        return Long.highestOneBit(value);
    }

    private static int ms1bPos(long value) {
        return Long.numberOfLeadingZeros(value);
    }
}
