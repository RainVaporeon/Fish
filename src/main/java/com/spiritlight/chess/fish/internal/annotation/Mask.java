package com.spiritlight.chess.fish.internal.annotation;

import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;

public enum Mask implements IntBinaryOperator, LongBinaryOperator {
    /**
     * Bitwise AND operation with the mask<br/>
     * This extracts the specified bits.
     */
    EXTRACT((l, r) -> l & r, (l, r) -> l & r),
    /**
     * Bitwise OR operation with the mask<br/>
     * This sets the specified bits to the on state.
     */
    SET((l, r) -> l | r, (l, r) -> l | r),
    /**
     * Bitwise XOR operation with the mask<br/>
     * This toggles the specified bits' states.
     */
    TOGGLE((l, r) -> l ^ r, (l, r) -> l ^ r),
    /**
     * Bitwise AND operation with the negated mask<br/>
     * This clears the masked bits.<br/>
     * This is a property of {@link Mask#EXTRACT}.
     */
    CLEAR((l, r) -> l & ~r, (l, r) -> l & ~r),
    /**
     * Bitwise OR operation with the negated mask<br/>
     * This sets bits other than the specified ones to the on state.<br/>
     * This is a property of {@link Mask#SET}.
     */
    SET_OTHER((l, r) -> l | ~r, (l, r) -> l | ~r),
    /**
     * Bitwise XOR operation with the negated mask<br/>
     * This toggles the state of bits other than the specified ones.<br/>
     * This is a property of {@link Mask#TOGGLE}.
     */
    TOGGLE_OTHER((l, r) -> l ^ ~r, (l, r) -> l ^ ~r),
    ;
    final IntBinaryOperator op;
    final LongBinaryOperator opL;

    Mask(IntBinaryOperator op, LongBinaryOperator opL) {
        this.op = op;
        this.opL = opL;
    }

    /**
     * Applies the value as an integer
     * @param value the value
     * @param mask the mask
     * @return the applied result
     * @apiNote it's recommended that one just do bitwise operations
     * instead of using this enum class. It may be useful for functions
     * though.
     */
    @Override
    public int applyAsInt(int value, int mask) {
        return op.applyAsInt(value, mask);
    }

    /**
     * Applies the value as a long
     * @param value the value
     * @param mask the mask
     * @return the applied result
     * @apiNote it's recommended that one just do bitwise operations
     * instead of using this enum class. It may be useful for functions
     * though.
     */
    @Override
    public long applyAsLong(long value, long mask) {
        return opL.applyAsLong(value, mask);
    }
}
