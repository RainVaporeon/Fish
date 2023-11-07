package com.spiritlight.chess.fish.game.utils.game;

public record Move(int from, int to) {

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

    public static Move of(String from, String to) {
        int file = from.charAt(0) - 'a';
        int rank = from.charAt(1) - '1';
        int fromPos = file + rank * 8;

        int toFile = to.charAt(0) - 'a';
        int toRank = to.charAt(1) - '1';
        int toPos = toFile + toRank * 8;
        return new Move(fromPos, toPos);
    }

    public static Move of(String fromTo) {
        String[] arr = fromTo.split(",");
        return of(arr[0].trim(), arr[1].trim());
    }

    @Override
    public String toString() {
        int fromFile = from % 8;
        int fromRank = from / 8;
        int toFile = to % 8;
        int toRank = to / 8;
        return "Move[" + (char) (fromFile + 'a') + (char) (fromRank + '1') +
                ',' + ' ' +
                (char) (toFile + 'a') + (char) (toRank + '1') +
                ']';
    }
}
