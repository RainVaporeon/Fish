package com.spiritlight.chess.fish.game.utils.board;

import com.spiritlight.chess.fish.game.utils.BitboardMask;

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
