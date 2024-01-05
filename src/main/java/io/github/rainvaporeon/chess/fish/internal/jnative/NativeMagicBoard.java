/*
 * The native header (magic_bits.hpp) is provided by goutham/magic-bits
 * (https://github.com/goutham/magic-bits/)
 */
package io.github.rainvaporeon.chess.fish.internal.jnative;

import java.io.*;

public class NativeMagicBoard {

    static {
        File tmp = new File(new File(System.getProperty("java.io.tmpdir")), "native_mbb.dll");
        InputStream stream = NativeMagicBoard.class.getResourceAsStream("/native/native_mbb.dll");
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(stream.readAllBytes());
            stream.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        System.loadLibrary(tmp.getAbsolutePath());
        tmp.deleteOnExit();
    }

    public static long getQueen(long blockers, int pos) {
        if(pos < 0 || pos >= 64) throw new IndexOutOfBoundsException(pos);
        return queen(blockers, pos);
    }

    public static long getRook(long blockers, int pos) {
        if(pos < 0 || pos >= 64) throw new IndexOutOfBoundsException(pos);
        return rook(blockers, pos);
    }

    public static long getBishop(long blockers, int pos) {
        if(pos < 0 || pos >= 64) throw new IndexOutOfBoundsException(pos);
        return bishop(blockers, pos);
    }

    private static native long queen(long blockers, int pos);

    private static native long rook(long blockers, int pos);

    private static native long bishop(long blockers, int pos);

}
