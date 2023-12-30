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

    // N, NW, W, SW, S, SE, E, NE
    private static final int[] POSITION_OFFSETS = {8, 7, -1, -9, -8, -7, 1, 9};

    public static final int NORTH = 8, NORTHWEST = 7, WEST = -1, SOUTHWEST = -9, SOUTH = -8, SOUTHEAST = -7, EAST = 1, NORTHEAST = 9;

    // Ray attack to that position
    private static final long[] rayN  = populate(POSITION_OFFSETS[0]);
    private static final long[] rayNW = populate(POSITION_OFFSETS[1]);
    private static final long[] rayW  = populate(POSITION_OFFSETS[2]);
    private static final long[] raySW = populate(POSITION_OFFSETS[3]);
    private static final long[] rayS  = populate(POSITION_OFFSETS[4]);
    private static final long[] raySE = populate(POSITION_OFFSETS[5]);
    private static final long[] rayE  = populate(POSITION_OFFSETS[6]);
    private static final long[] rayNE = populate(POSITION_OFFSETS[7]);

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

    private static long[] populate(int offset) {
        long[] arr = new long[64];
        for(int i = 0; i < 64; i++) {
            long mask = 0;
            int pos = i + offset;
            while(pos >= 0 && pos < 64) {
                mask |= 1L << pos;
                pos += offset;
            }
            arr[i] = mask;
        }
        return arr;
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
        return switch (pos) {
            case 8 -> rayN[offset];
            case 7 -> rayNW[offset];
            case -1 -> rayW[offset];
            case -9 -> raySW[offset];
            case -8 -> rayS[offset];
            case -7 -> raySE[offset];
            case 1 -> rayE[offset];
            case 9 -> rayNE[offset];
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
