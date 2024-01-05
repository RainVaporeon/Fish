package io.github.rainvaporeon.chess.fish.game.utils.board;

public class BoardInfo implements Cloneable {
    /* package-private */ int enPassantSquare = 0;
    // Uses: WHITE, BLACK (0, 1)
    /* package-private */ int turn = 0;
    /* package-private */ int halfMove = 0;
    /* package-private */ int fullMove = 1;

    @Override
    public String toString() {
        return String.format("""
                enPassant=%s,
                turn=%s,
                halfMove=%s,
                fullMove=%s
                """, enPassantSquare, turn, halfMove, fullMove);
    }

    @Override
    public BoardInfo clone() {
        try {
            return (BoardInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
