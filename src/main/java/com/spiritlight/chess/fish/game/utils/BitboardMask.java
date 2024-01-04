package com.spiritlight.chess.fish.game.utils;

public class BitboardMask {
    public static final long FILE_MASK = 0x0101010101010101L;
    public static long getFileMask(int file) {
        return FILE_MASK << file;
    }

    /**
     * Returns the three files, centered by the specified file,
     * combined with an offset of one, and truncate if the value
     * is less than 0 or larger than 7.
     * @param file the centered file
     * @return a mask for left, middle and right file.
     */
    public static long getCenteredFileMask(int file) {
        int left = Math.max(file, 0);
        int right = Math.min(file, 7);

        return getFileMask(left) | getFileMask(file) | getFileMask(right);
    }

    /**
     * Returns the mask, covering all ranks higher than the specified rank.
     * @param rank the rank
     * @return the mask
     */
    public static long getForwardMask(int rank, boolean backwards) {
        if(backwards) return -1L >>> (8 * rank - 1);
        return -1L << (8 * rank + 1);
    }
}
