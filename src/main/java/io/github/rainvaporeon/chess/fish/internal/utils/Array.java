package io.github.rainvaporeon.chess.fish.internal.utils;

import com.spiritlight.fishutils.misc.annotations.Modifies;
import com.spiritlight.fishutils.misc.annotations.New;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/**
 * Helper class which keeps a collection of static methods
 * to assist in modification of scoreboards.
 */
public class Array {

    /**
     * Inverts the given score board
     * @param array the array
     */
    @Modifies("array")
    public static void invert(int[] array) {
        for(int i = 0; i < array.length; i++) {
            array[i] ^= 0xFFFFFFFF;
        }
    }

    /**
     * Retains the given score board and returns an inverted version of it
     * @param array the array, not modified in the process.
     * @return the inverted array
     */
    @Modifies(Modifies.NONE)
    public static @New int[] invertAndRetain(int[] array) {
        int[] ret = array.clone();
        Array.invert(ret);
        return ret;
    }

    /**
     * Multiplies the array by given modifier
     * @param array the array
     * @param modifier the modifier
     * @param halfUp whether to round as half-up, if false, trailing decimals
     *               are truncated.
     */
    @Modifies("array")
    public static void multiply(int[] array, double modifier, boolean halfUp) {
        for(int i = 0; i < array.length; i++) {
            array[i] = (int) ((array[i] * modifier) + (halfUp ? 0.5 : 0));
        }
    }

    /**
     * Multiplies the array by given modifier
     * @param array the array, unmodified in the process
     * @param modifier the modifier
     * @param halfUp whether to round as half-up, if false, trailing decimals
     *               are truncated.
     * @return a new instance of the array, multiplied by the modifier
     */
    public static @New int[] multiplyAndRetain(int[] array, double modifier, boolean halfUp) {
        int[] ret = array.clone();
        multiply(ret, modifier, halfUp);
        return ret;
    }

    /**
     * Maps each element in this array to a new value
     * @param array the array to be mutated
     * @param op the mapper
     */
    @Modifies("array")
    public static void map(int[] array, IntUnaryOperator op) {
        for(int i = 0; i < array.length; i++) {
            array[i] = op.applyAsInt(array[i]);
        }
    }
    /**
     * Maps each element in this array to a new value
     * @param array the array
     * @param op the mapper
     * @return the mapped array
     */
    public static @New int[] mapAndRetain(int[] array, IntUnaryOperator op) {
        int[] ret = array.clone();
        map(ret, op);
        return ret;
    }

    /**
     * Maps each element in this array to a new value
     * @param array the array
     * @param op the mapper
     * @return the mapped array
     * @apiNote the operators are ordered. That is, the application starts at the
     * first operator, and ends with the last one.
     */
    public static @New int[] mapAndRetainMulti(int[] array, IntUnaryOperator... op) {
        int[] ret = array.clone();
        for(var v : op) {
            map(ret, v);
        }
        return ret;
    }

    /**
     * Replaces each element in this array to be the average value.
     * Decimals are truncated, but round up if the halfUp boolean is true.
     * @param array the array
     * @param halfUp whether to round half-up
     */
    @Modifies("array")
    public static void average(int[] array, boolean halfUp) {
        double avg = 0;
        for(int i : array) avg += i;
        avg /= array.length;
        int fill = (int) (halfUp ? avg + 0.5 : avg);
        Arrays.fill(array, fill);
    }

    /**
     * Copies this array and replaces each element in the new array to be
     * the average value.
     * Decimals are truncated, but round up if the halfUp boolean is true.
     * @param array the array
     * @param halfUp whether to round half-up
     */
    public static @New int[] averageAndRetain(int[] array, boolean halfUp) {
        int[] r = array.clone();
        average(array, halfUp);
        return r;
    }
}
