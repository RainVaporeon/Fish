package io.github.rainvaporeon.chess.fish.internal.utils;

import io.github.rainvaporeon.chess.fish.game.Piece;
import io.github.rainvaporeon.chess.fish.game.utils.board.AttackTable;
import io.github.rainvaporeon.chess.fish.game.utils.board.Magic;
import com.spiritlight.fishutils.collections.IntList;
import io.github.rainvaporeon.chess.fish.internal.jnative.MagicBitboard;

import static io.github.rainvaporeon.chess.fish.game.Piece.BISHOP;
import static io.github.rainvaporeon.chess.fish.game.Piece.ROOK;

public class Bits {
    private static final double LOG_2 = Math.log(2);

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

    public static long getRayAttack(long blocking, int pos, int piece) {
        if(Piece.is(piece, ROOK)) return getRookRay(blocking, pos);
        if(Piece.is(piece, Piece.BISHOP)) return getBishopRay(blocking, pos);
        if(Piece.is(piece, Piece.QUEEN)) return getQueenRay(blocking, pos);
        return 0;
    }

    public static long getRayAttackMagic(long blocking, int pos, int piece) {
        if(Piece.is(piece, ROOK)) return getRookRayMagic(blocking, pos);
        if(Piece.is(piece, Piece.BISHOP)) return getBishopRayMagic(blocking, pos);
        if(Piece.is(piece, Piece.QUEEN)) return getQueenRayMagic(blocking, pos);
        return 0;
    }

    private static long getRookRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return MagicBitboard.getRook(blocking, pos);
        long attack = AttackTable.getDirect(ROOK, pos);
        long magic = Magic.get(ROOK, pos);
        int shiftCount = 64 - Long.bitCount(magic);
        return ((attack & blocking) * magic) >>> shiftCount;
    }

    private static long getBishopRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return MagicBitboard.getBishop(blocking, pos);
        long attack = AttackTable.getDirect(BISHOP, pos);
        long magic = Magic.get(BISHOP, pos);
        int shiftCount = 64 - Long.bitCount(magic);
        return ((attack & blocking) * magic) >>> shiftCount;
    }

    private static long getQueenRayMagic(long blocking, int pos) {
        if(type == Type.NATIVE) return MagicBitboard.getQueen(blocking, pos);
        return getRookRayMagic(blocking, pos) | getBishopRayMagic(blocking, pos);
    }

    private static long getRookRay(long blocking, int pos) {
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

    private static long getBishopRay(long blocking, int pos) {
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

    private static long getQueenRay(long blocking, int pos) {
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
