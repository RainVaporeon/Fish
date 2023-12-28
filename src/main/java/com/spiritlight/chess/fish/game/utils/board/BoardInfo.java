package com.spiritlight.chess.fish.game.utils.board;

public class BoardInfo {
    /* package-private */ int enPassantSquare;
    // Uses: WHITE, BLACK (0, 1)
    /* package-private */ int turn;
    /* package-private */ int halfMove;
    /* package-private */ int fullMove = 1;

    @Override
    public String toString() {
        return STR."BoardInfo{enPassantSquare=\{enPassantSquare}, turn=\{turn}, halfMove=\{halfMove}, fullMove=\{fullMove}\{'}'}";
    }
}
