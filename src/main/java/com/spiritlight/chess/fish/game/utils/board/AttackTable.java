package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.fishutils.collections.IntList;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

import static com.spiritlight.chess.fish.game.Piece.*;

public class AttackTable {
    private static final LongArray knight;
    private static final LongArray bishop;
    private static final LongArray rook;
    private static final LongArray queen;
    private static final LongArray king;

    // N, NW, W, SW, S, SE, E, NE
    private static final int[] POSITION_OFFSETS = {8, 7, -1, -9, -8, -7, 1, 9};

    public static final int NORTH = 8, NORTHWEST = 7, WEST = -1, SOUTHWEST = -9, SOUTH = -8, SOUTHEAST = -7, EAST = 1, NORTHEAST = 9;

    // Ray attack to that position
    private static final long[] rayN  = populateStraight(POSITION_OFFSETS[0]);
    private static final long[] rayNW = populateDiagonal(POSITION_OFFSETS[1]);
    private static final long[] rayW  = populateStraight(POSITION_OFFSETS[2]);
    private static final long[] raySW = populateDiagonal(POSITION_OFFSETS[3]);
    private static final long[] rayS  = populateStraight(POSITION_OFFSETS[4]);
    private static final long[] raySE = populateDiagonal(POSITION_OFFSETS[5]);
    private static final long[] rayE  = populateStraight(POSITION_OFFSETS[6]);
    private static final long[] rayNE = populateDiagonal(POSITION_OFFSETS[7]);

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
            int currentRank = BoardHelper.getRank(i);
            int currentFile = BoardHelper.getFile(i);
            // dirty thing, though this only runs once on start, so it's probably fine
            for(int offset : kingOffsets) {
                int position = i + offset;
                if(position < 0 || position > 63) continue;
                int rank = BoardHelper.getRank(position);
                int file = BoardHelper.getFile(position);
                int rankDiff = Math.abs(rank - currentRank);
                int fileDiff = Math.abs(file - currentFile);
                if(rankDiff == 1) {
                    if(fileDiff <= 1) value |= 1L << position;
                }
                if(fileDiff == 1) {
                    if(rankDiff <= 1) value |= 1L << position;
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

    private static long[] populateDiagonal(int offset) {
        long[] array = new long[64];
        for(int i = 0; i < 64; i++) {
            array[i] = getDiagonalSliding(i, offset);
        }
        return array;
    }

    private static long getDiagonalSliding(int pos, int offset) {
        int idx = 1; // Starting at 1 excludes itself
        int file = BoardHelper.getFile(pos);
        int rank = BoardHelper.getRank(pos);
        IntList list = new IntList(8);
        while (true) {
            int x, y;
            switch (offset) {
                case NORTHWEST -> {
                    x = file - idx;
                    y = rank + idx;
                }
                case NORTHEAST -> {
                    x = file + idx;
                    y = rank + idx;
                }
                case SOUTHWEST -> {
                    x = file - idx;
                    y = rank - idx;
                }
                case SOUTHEAST -> {
                    x = file + idx;
                    y = rank - idx;
                }
                default -> throw new IllegalStateException(STR."Unexpected value: \{offset}");
            }
            if(x < 0 || x > 7 || y < 0 || y > 7) {
                long ret = 0;
                for(int value : list) ret |= 1L << value;
                return ret;
            } else {
                list.add(x + 8 * y);
                idx++;
            }
        }
    }

    // what's the opposite of diagonal? PR for the correct name
    private static long[] populateStraight(int offset) {
        long[] array = new long[64];
        for(int i = 0; i < 64; i++) {
            int index = i + offset;
            int initialFile = BoardHelper.getFile(index);
            int initialRank = BoardHelper.getRank(index);
            long mask = 0;

            while(index >= 0 && index < 64 && // within bounds, or the index stays consistent to rank/index
                    (BoardHelper.getFile(index) == initialFile || BoardHelper.getRank(index) == initialRank)) {
                long l = 1L << index;
                mask |= l;
                index += offset;
            }
            array[i] = mask;
        }
        return array;
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

    public static long getRay(int pos, int offset) {
        if(pos == 64) return 0; // non-conflicting with blockers
        return switch (offset) {
            case 8 -> rayN[pos];
            case 7 -> rayNW[pos];
            case -1 -> rayW[pos];
            case -9 -> raySW[pos];
            case -8 -> rayS[pos];
            case -7 -> raySE[pos];
            case 1 -> rayE[pos];
            case 9 -> rayNE[pos];
            default -> throw new IllegalArgumentException(STR."for input pos:offset: \{pos}:\{offset}");
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
        return switch (piece & PIECE_MASK) {
            case QUEEN -> directQueen[position];
            case ROOK -> directRook[position];
            case BISHOP -> directBishop[position];
            case KNIGHT -> directKnight[position];
            case KING -> directKing[position];
            default -> 0;
            // TODO: Pawns do not have a mask here, and may be used
            // TODO: for check detection, causing an issue where
            // TODO: pawns giving check can be ignored.
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
