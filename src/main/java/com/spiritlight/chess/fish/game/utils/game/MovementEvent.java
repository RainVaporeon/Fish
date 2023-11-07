package com.spiritlight.chess.fish.game.utils.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.spiritlight.chess.fish.game.Piece.*;

/**
 * A movement event, used as a return value from {@link com.spiritlight.chess.fish.game.utils.board.BoardMap#update(Move)}.
 * <p>
 * This class contains information about the last move,
 * describing the capturing piece, captured piece and the move provided.
 * Alternatively, the piece fields may be used as an error code; reasons
 * are then obtainable upon converting that specific event to String with
 * toString().
 */
public class MovementEvent {
    // reserved for translation
    private static final Map<Integer, MovementEvent> errorMap = new HashMap<>();

    // Static collection of move reasons
    /**
     * Move that is not possible, let it be something blocking the path,
     * or the move is not possible to be reached.
     */
    public static final MovementEvent ILLEGAL = createError(0xFFFFFFFF, "Illegal move");
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

    /* Private constructor reserved for errors */
    private MovementEvent(int code, Move move) {
        this.capturingPiece = code;
        this.capturedPiece = code;
        this.move = move;
    }

    public MovementEvent(int capturingPiece, int capturedPiece, Move move) {
        this.capturingPiece = capturingPiece;
        this.capturedPiece = capturedPiece;
        this.move = Objects.requireNonNull(move);
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
     * @return whether move is not null.
     */
    public boolean validate() {
        return move != null;
    }

    public boolean illegal() {
        System.out.println("Movement=" + getReason(this) + " | " + this);
        return move == null;
    }

    public int code() {
        return capturingPiece;
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

    public static MovementEvent getTranslationCode(int code) {
        return errorMap.get(code);
    }

    private static MovementEvent createError(int code, String message) {
        MovementEvent event = new MovementEvent(code, null) {
            @Override
            public String toString() {
                return message + " (Code " + Integer.toHexString(code) + ")";
            }
        };
        errorMap.put(code, event);
        return event;
    }
}
