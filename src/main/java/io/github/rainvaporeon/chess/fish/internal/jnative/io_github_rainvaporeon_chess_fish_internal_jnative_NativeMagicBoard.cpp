#include "magic-bits/include/magic_bits.hpp"
#include "io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard.h"
#include "jni.h"

const magic_bits::Attacks attacks = magic_bits::Attacks();

/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard
 * Method:    queen
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard_queen
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return attacks.Queen(blockers, pos);
}
/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard
 * Method:    rook
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard_rook
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return attacks.Rook(blockers, pos);
}
/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard
 * Method:    bishop
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_NativeMagicBoard_bishop
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return attacks.Bishop(blockers, pos);
}
