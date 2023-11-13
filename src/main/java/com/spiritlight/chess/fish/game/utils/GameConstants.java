package com.spiritlight.chess.fish.game.utils;

import com.spiritlight.chess.fish.internal.annotation.Mask;
import com.spiritlight.chess.fish.internal.annotation.MaskType;

public class GameConstants {
    /**
     * Mask to check whether the white king may castle.<br/>
     * If this evaluates to {@code 0}, the king cannot castle
     * either side.
     */
    @MaskType(Mask.EXTRACT) @FENConstant
    public static final int WHITE_CASTLE_MASK = 0b00000011;

    /**
     * Mask to check whether the black king may castle.<br/>
     * If this evaluates to {@code 0}, the king cannot castle
     * either side.
     */
    @MaskType(Mask.EXTRACT) @FENConstant
    public static final int BLACK_CASTLE_MASK = 0b00001100;

    @FENConstant
    public static final int WHITE_CASTLE_KING_SIDE  = 0b00000001;
    @FENConstant
    public static final int WHITE_CASTLE_QUEEN_SIDE = 0b00000010;

    @FENConstant
    public static final int BLACK_CASTLE_KING_SIDE  = 0b00000100;
    @FENConstant
    public static final int BLACK_CASTLE_QUEEN_SIDE = 0b00001000;

    @FENConstant
    public static final int WHITE_TURN = 0b00000000;
    @FENConstant
    public static final int BLACK_TURN = 0b00000001;

    public static final double SQRT_5 = Math.sqrt(5);

}
