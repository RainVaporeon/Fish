/*
 * The native header (magic_bits.hpp) is provided by goutham/magic-bits
 * (https://github.com/goutham/magic-bits/)
 */
package io.github.rainvaporeon.chess.fish.internal.jnative;

public class MagicBitboard {

    static {
        /* Load the native library */
    }

    public native long queen(long blockers, int pos);

    public native long rook(long blockers, int pos);

    public native long bishop(long blockers, int pos);

}
