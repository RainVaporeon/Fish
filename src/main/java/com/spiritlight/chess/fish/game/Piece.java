package com.spiritlight.chess.fish.game;

import com.spiritlight.chess.fish.game.utils.board.BoardHelper;
import com.spiritlight.chess.fish.internal.InternLogger;
import com.spiritlight.chess.fish.internal.annotation.Mask;
import com.spiritlight.chess.fish.internal.annotation.MaskType;
import com.spiritlight.chess.fish.internal.exceptions.SystemError;

import static com.spiritlight.chess.fish.game.utils.game.Move.FORWARD_OFFSET;

public class Piece {
    public static final int NONE   = 0b00000000;
    public static final int PAWN   = 0b00000001;
    public static final int KNIGHT = 0b00000010;
    public static final int BISHOP = 0b00000011;
    public static final int ROOK   = 0b00000100;
    public static final int QUEEN  = 0b00000101;
    public static final int KING   = 0b00000110;

    public static final int WHITE = 0b00001000;
    public static final int BLACK = 0b00010000;

    @MaskType({Mask.EXTRACT, Mask.CLEAR})
    public static final int COLOR_MASK = 0b00011000;

    @MaskType(Mask.EXTRACT)
    public static final int PIECE_MASK = 0b00000111;

    /**
     * Checks whether the piece is of the type
     * @param piece the piece
     * @param type the type
     * @return true if the piece is of the type, false otherwise
     */
    public static boolean is(int piece, int type) {
        return (piece & type) != 0;
    }

    /**
     * Retrieves the color of this piece
     * @param piece the piece
     * @return the color
     */
    public static int color(int piece) {
        return piece & COLOR_MASK;
    }

    public static boolean isSlidingPiece(int piece) {
        return piece == BISHOP || piece == ROOK || piece == QUEEN;
    }

    /**
     * Generates sliding moves for the given sliding piece
     * at the given location
     * @param piece the piece
     * @param src the source location, ranges from 0 to 63
     * @return an int array describing the sliding squares it can
     * go to
     */
    public static int[] sliding(int piece, int src) {
        if (!Piece.isSlidingPiece(piece)) throw new SystemError("Unexpected call to sliding() with parameter " + piece + ", " + src);
        if (piece == PAWN) return new int[]{src + FORWARD_OFFSET};
        int file = BoardHelper.getFile(src);
        int rank = BoardHelper.getRank(src);
        int[] ret = new int[determineSize(piece, file, rank)];
        int arrIdx = 0;
        if (piece == ROOK || piece == QUEEN) {
            for (int i = 0; i < 8; i++) {
                if (i != file) {
                    ret[arrIdx++] = i + rank * 8; // L-R
                }
                if (i != rank) {
                    ret[arrIdx++] = file * i * 8; // D-U
                }
            }
        }
        if (piece == BISHOP || piece == QUEEN) {
            int mode = 0; // 0: L-T, 1: R-T, 2: L-D, 3: R-D (left-top etc.)
            int idx = 1; // Starting at 1 excludes itself
            while (mode >= 0 && mode < 4) {
                int x, y;
                switch (mode) {
                    case 0 -> {
                        x = file - idx;
                        y = rank + idx;
                    }
                    case 1 -> {
                        x = file + idx;
                        y = rank + idx;
                    }
                    case 2 -> {
                        x = file - idx;
                        y = rank - idx;
                    }
                    case 3 -> {
                        x = file + idx;
                        y = rank - idx;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mode);
                }
                InternLogger.getLogger().debug("Generated move " + x + ", " + y + " at src " + src + " for piece " + asString(piece));
                if(x < 0 || x > 7 || y < 0 || y > 7) {
                    InternLogger.getLogger().debug("Illegal move, skipping it...");
                    idx = 1; // Start at 1 to exclude itself
                    mode++;
                } else {
                    ret[arrIdx++] = x + 8 * y;
                    idx++;
                }
            }
        }
        return ret;
    }

    /**
     * Internal method to determine the expected reachable squares
     * by the given piece and location on board
     * @param piece the piece
     * @param x the file
     * @param y the rank
     * @return maximum amounts of moves this piece can make
     */
    private static int determineSize(int piece, int x, int y) {
        if(piece == BISHOP) return bishopReachableSquare(x, y);
        if(piece == ROOK) return 14;
        // TODO: Implement knights
        return 14 + bishopReachableSquare(x, y);
    }

    private static int bishopReachableSquare(int x, int y) {
        x++;y++;
        int tl = Math.min(x, y) - 1;
        int tr = Math.min(x, 9 - y);
        int bl = 8 - Math.max(x, 9 - y);
        int br = 8 - Math.max(x, y) - 1;
        return tl + tr + bl + br;
    }

    /**
     *
     * @param piece piece type
     * @return the FEN equivalent of the piece, or '?' if the input
     * is not of a piece type.
     */
    public static char asCharacter(int piece) {
        char c = switch (piece & ~COLOR_MASK) {
            case NONE -> ' ';
            case PAWN -> 'P';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case ROOK -> 'R';
            case QUEEN -> 'Q';
            case KING -> 'K';
            default -> '?';
        };

        if((piece & COLOR_MASK) == WHITE) return c;
        return Character.toLowerCase(c);

    }

    /**
     * Converts the string integer representation of this piece
     * to the string representation of this string
     * @param piece the piece
     * @return the string representation of this piece, or an
     * empty string if the input is not a number
     * @apiNote this is same as calling {@code Piece#asString(Integer.parseInt(piece))}
     * @see Piece#asString(int)
     */
    public static String asString(String piece) {
        try {
            return asString(Integer.parseInt(piece));
        } catch (NumberFormatException ex) {
            return "";
        }
    }

    /**
     * Converts the given piece into its string representation.
     *
     * The conversion result follows this rule: <pre>
     *     If the 4th bit is on... append "White "
     *     If the 5th bit is on... append "Black "
     *     If neither bits are on... append "No-Color "
     *     then...
     *     Retrieve the last 3 bits and converts to the
     *     value specified, or "Unknown Piece Type {}"
     * </pre>
     * where {} is replaced with the last 3 bits and
     * the hexadecimal representation of the input piece
     * @param piece the piece
     * @return the string representation of this piece
     */
    public static String asString(int piece) {
        StringBuilder builder = new StringBuilder();
        if((piece & WHITE) != 0) builder.append("White").append(" ");
        if((piece & BLACK) != 0) builder.append("Black").append(" ");
        if((piece & COLOR_MASK) == 0) builder.append("No-Color").append(" ");
        int filter = piece & PIECE_MASK;
        switch (filter) {
            case PAWN ->   builder.append("Pawn");
            case BISHOP -> builder.append("Bishop");
            case KNIGHT -> builder.append("Knight");
            case ROOK ->   builder.append("Rook");
            case QUEEN ->  builder.append("Queen");
            case KING ->   builder.append("King");
            default ->     builder.append("Unknown Piece Type ").append(filter).append(" ").append(Integer.toHexString(piece));
        }
        return builder.toString();
    }
}
