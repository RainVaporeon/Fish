package com.spiritlight.chess.fish.game.utils.board;

public class BoardInfo {
    /* package-private */ int enPassantSquare = 0;
    // Uses: WHITE, BLACK (0, 1)
    /* package-private */ int turn = 0;
    /* package-private */ int halfMove = 0;
    /* package-private */ int fullMove = 1;

    @Override
    public String toString() {
        return STR."BoardInfo{enPassantSquare=\{enPassantSquare}, turn=\{turn}, halfMove=\{halfMove}, fullMove=\{fullMove}\{'}'}";
    }
}
