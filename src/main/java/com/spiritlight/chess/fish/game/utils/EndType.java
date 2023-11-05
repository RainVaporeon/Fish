package com.spiritlight.chess.fish.game.utils;

public enum EndType {
    WHITE_WIN_CHECKMATE,
    BLACK_WIN_CHECKMATE,

    WHITE_WIN_TIMEOUT,
    BLACK_WIN_TIMEOUT,

    DRAW_AGREEMENT,
    DRAW_STALEMATE,
    /**
     * This rule occurs if the half-move clock accumulates to 100.
     */
    DRAW_50_MOVE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_TIMEOUT_INSUFFICIENT_MATERIAL,

    /**
     * Indicates that the game is still in progress.
     */
    IN_PROGRESS
}
