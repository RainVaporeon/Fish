package com.spiritlight.chess.fish.game;

import com.spiritlight.chess.fish.internal.annotation.Mask;
import com.spiritlight.chess.fish.internal.annotation.MaskType;

public class Piece {
    public static final int NONE = 0b00000000;
    public static final int PAWN = 0b00000001;
    public static final int KNIGHT = 0b00000010;
    public static final int BISHOP = 0b00000011;
    public static final int ROOK = 0b00000100;
    public static final int QUEEN = 0b00000101;
    public static final int KING = 0b00000110;

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
     * @apiNote the parameters are interchangeable.
     */
    public static boolean is(int piece, int type) {
        return (piece & type) != 0;
    }

    public static String asString(int piece) {
        StringBuilder builder = new StringBuilder();
        if((piece & WHITE) != 0) builder.append("White").append(" ");
        if((piece & BLACK) != 0) builder.append("Black").append(" ");
        if((piece & COLOR_MASK) == 0) builder.append("No-Color").append(" ");
        int filter = piece & ~COLOR_MASK;
        switch (filter) {
            case PAWN -> builder.append("Pawn");
            case BISHOP -> builder.append("Bishop");
            case KNIGHT -> builder.append("Knight");
            case ROOK -> builder.append("Rook");
            case QUEEN -> builder.append("Queen");
            case KING -> builder.append("King");
            default -> builder.append("Unknown Piece ").append(filter);
        }
        return builder.toString();
    }
}
