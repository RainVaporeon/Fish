#include "magic-bits/include/magic_bits.hpp"
#include "jni.h"
/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard
 * Method:    queen
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard_queen
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return magic_bits::Attacks().Queen(blockers, pos);
}

/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard
 * Method:    rook
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard_rook
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return magic_bits::Attacks().Rook(blockers, pos);
}

/*
 * Class:     io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard
 * Method:    bishop
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_io_github_rainvaporeon_chess_fish_internal_jnative_MagicBitboard_bishop
        (JNIEnv *, jclass, jlong blockers, jint pos) {
    return magic_bits::Attacks().Bishop(blockers, pos);
}
