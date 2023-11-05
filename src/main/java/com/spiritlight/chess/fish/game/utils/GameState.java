package com.spiritlight.chess.fish.game.utils;

public enum GameState {
    EARLY_GAME,
    MIDDLE_GAME,
    /**
     * The endgame phase, not to be confused with {@link GameState#GAME_END}
     */
    END_GAME,
    /**
     * The end of the game, not to be confused with {@link GameState#END_GAME}
     */
    GAME_END
}
