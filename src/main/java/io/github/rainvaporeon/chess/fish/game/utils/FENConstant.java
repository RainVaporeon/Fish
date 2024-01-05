package io.github.rainvaporeon.chess.fish.game.utils;

import java.lang.annotation.*;

/**
 * Constant denoting that this field is used for FEN-related operations.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface FENConstant {
}
