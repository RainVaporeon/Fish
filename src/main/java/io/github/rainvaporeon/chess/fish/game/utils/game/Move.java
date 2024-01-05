package io.github.rainvaporeon.chess.fish.game.utils.game;

/**
 * Denoting a move
 * @param from the square from
 * @param to the square to
 */
public record Move(int from, int to) {

    // As we store this in a long usually, a rank forward
    // is 8 units, and a file forward is one unit.
    public static final int FORWARD_OFFSET = 8;
    public static final int BACKWARD_OFFSET = -8;
    public static final int LEFT_OFFSET = -1;
    public static final int RIGHT_OFFSET = 1;

    public Move up() {
        return new Move(from, to + FORWARD_OFFSET);
    }

    public Move down() {
        return new Move(from, to + BACKWARD_OFFSET);
    }

    public Move left() {
        return new Move(from, to + LEFT_OFFSET);
    }

    public Move right() {
        return new Move(from, to + RIGHT_OFFSET);
    }

    public int sourcePos() {
        return from;
    }

    public int destPos() {
        return to;
    }

    public Move invert() {
        return new Move(to, from);
    }

    /**
     * Converts the string to a move. Note that
     * the converted position does <b>not</b> correspond
     * to the code used in {@link io.github.rainvaporeon.chess.fish.game.FEN}.
     * @param from the move from
     * @param to the move to
     * @return a move describing the move.
     */
    public static Move of(String from, String to) {
        int file = from.charAt(0) - 'a';
        int rank = from.charAt(1) - '1';
        int fromPos = file + rank * 8;

        int toFile = to.charAt(0) - 'a';
        int toRank = to.charAt(1) - '1';
        int toPos = toFile + toRank * 8;
        return new Move(fromPos, toPos);
    }

    public static Move of(int from, int to) {
        return new Move(from, to);
    }

    public static Move of(String fromTo) {
        String[] arr = fromTo.split(",");
        return of(arr[0].trim(), arr[1].trim());
    }

    public static Move ofInverse(String fromTo) {
        String[] arr = fromTo.split(",");
        return of(arr[1].trim(), arr[0].trim());
    }

    public static String parseLocation(int src) {
        return STR."\{(char) ((src % 8) + 'a')}\{(char) ((src / 8) + '1')}";
    }

    public static int parseLocation(String src) {
        int file = src.charAt(0) - 'a';
        int rank = src.charAt(1) - '1';
        return file + rank * 8;
    }

    @Override
    public String toString() {
        int fromFile = from % 8;
        int fromRank = from / 8;
        int toFile = to % 8;
        int toRank = to / 8;
        return STR."Move[\{(char) (fromFile + 'a')}\{(char) (fromRank + '1')}\{','}\{' '}\{(char) (toFile + 'a')}\{(char) (toRank + '1')}\{']'}";
    }
}
