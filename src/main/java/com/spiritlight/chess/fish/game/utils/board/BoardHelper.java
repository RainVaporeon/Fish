package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.FEN;
import com.spiritlight.chess.fish.game.Piece;
import com.spiritlight.chess.fish.game.utils.BitboardMask;
import com.spiritlight.fishutils.misc.arrays.primitive.CharacterArray;

public class BoardHelper {
    /**
     * Gets the file of the position
     * @param position the position
     * @return the file, starting from 0
     */
    // File is like left and right
    public static int getFile(int position) {
        return position % 8;
    }

    /**
     * Gets the rank of the position
     * @param position the position
     * @return the rank, starting from 0
     */
    // Rank is like up and down
    public static int getRank(int position) {
        return position / 8;
    }

    public static String getPositionString(int position) {
        return STR."\{(char) ('a' + getFile(position))}\{(char) ('1' + getRank(position))}";
    }

    /**
     * Converts the position specified in {@link BoardMap} to
     * the kind stored in this engine's FEN parser system.
     * @param position The position
     * @return the FEN representation
     */
    public static int getFENPosition(int position) {
        return ((position % 8) << 4) + (position / 8);
    }

    /**
     * Converts the position specified in {@link FEN} to the kind stored in this
     * board map's position system
     * @param position the position
     * @return the board map representation
     */
    public static int fromFENPosition(int position) {
        return (position >> 4) + 8 * (position & 0x0F);
    }

    public static String viewBoard(String fen) {
        int[] data = FEN.load(fen);
        CharacterArray array = CharacterArray.create(64, size -> Piece.asCharacter(data[size]));
        return String.format("""
                + - + - + - + - + - + - + - + - +
                8 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                7 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                6 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                5 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                4 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                3 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                2 %s | %s | %s | %s | %s | %s | %s | %s |
                + - + - + - + - + - + - + - + - +
                1 %s | %s | %s | %s | %s | %s | %s | %s |
                + a + b + c + d + e + f + g + h +
                """, (Object[]) array.toArray());
    }

    /**
     * Visualizes the given long pattern in an 8x8 board
     * @param pattern the pattern
     * @return the bit board representation of the long
     */
    public static String visualize(long pattern) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                builder.append((pattern & 1L << i * 8 + j) == 0 ? 0 : 1);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Retrieves the mask for checking for passed pawns
     * @param position the position
     * @param backwards whether to check behind the position instead of forward
     * @return the mask
     */
    public static long getPassedPawnMask(int position, boolean backwards) {
        long forwardMask = BitboardMask.getForwardMask(getRank(position), backwards);
        long fileMask = BitboardMask.getCenteredFileMask(getFile(position));

        return forwardMask & fileMask;
    }
}
