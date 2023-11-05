package com.spiritlight.chess.fish.internal.annotation;

import java.lang.annotation.*;

/**
 * Indicates the masking type. The following table indicates
 * which bitwise operations are used:
 * <pre>
 * {@link Mask#EXTRACT}: AND operation
 * {@link Mask#CLEAR}: AND operation with negated mask
 * {@link Mask#SET}: OR operation
 * {@link Mask#TOGGLE}: XOR operation
 * </pre>
 * @apiNote This annotation merely gives a flag to the annotated
 * field for the suggested uses. Although this is often used to
 * indicate the masking type of this bit mask, it does not have
 * any actual action to prevent using another masking other than
 * the annotated types
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface MaskType {
    Mask[] value();
}
