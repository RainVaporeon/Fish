package com.spiritlight.chess.fish.game.utils.game;

import java.util.Objects;

import static com.spiritlight.chess.fish.game.Piece.*;

public class MovementEvent {
    // Static collection of move reasons
    /**
     * Move that is not possible, let it be something blocking the path,
     * or the move is not possible to be reached.
     */
    public static final MovementEvent ILLEGAL = createError(NONE, "Illegal move");
    /**
     * Move that lands on something that has the same color as the
     * source piece
     */
    public static final MovementEvent CAPTURING_SAME = createError(0x10000001, "Capturing same color piece");
    /**
     * Move that will expose a check to the piece's color's king
     */
    public static final MovementEvent REVEALS_CHECK = createError(0x10000002, "Move reveals a check on king");
    /**
     * It's white to play, but a black piece was moved
     */
    public static final MovementEvent WHITE_TO_PLAY = createError(WHITE, "White to play (Wrong move target color)");
    /**
     * It's black to play, but a white piece was moved
     */
    public static final MovementEvent BLACK_TO_PLAY = createError(BLACK, "Black to play (Wrong move target color)");

    private final int capturingPiece;
    private final int capturedPiece;
    private final Move move;

    public MovementEvent(int capturingPiece, int capturedPiece, Move move) {
        this.capturingPiece = capturingPiece;
        this.capturedPiece = capturedPiece;
        this.move = move;
    }

    public static String getReason(MovementEvent src) {
        return src.move == null ? src.toString() : "null";
    }

    /**
     * Validates this move.
     * The validity of a move is determined
     * by this event's "move" field.
     * If it's not null, this indicates
     * that the move was made, not otherwise.
     *
     * @return whether move is null.
     */
    public boolean validate() {
        return move != null;
    }

    public boolean illegal() {
        System.out.println("Movement=" + getReason(this) + " | " + this);
        return move == null;
    }

    public int capturingPiece() {
        return capturingPiece;
    }

    public int capturedPiece() {
        return capturedPiece;
    }

    public Move move() {
        return move;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MovementEvent) obj;
        return this.capturingPiece == that.capturingPiece &&
                this.capturedPiece == that.capturedPiece &&
                Objects.equals(this.move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capturingPiece, capturedPiece, move);
    }

    @Override
    public String toString() {
        return "MovementEvent[" +
                "capturingPiece=" + capturingPiece + ", " +
                "capturedPiece=" + capturedPiece + ", " +
                "move=" + move + ']';
    }

    private static MovementEvent createError(int code, String message) {
        return new MovementEvent(code, code, null) {
            @Override
            public String toString() {
                return message + "(Code " + Integer.toHexString(code) + ")";
            }
        };
    }
}
