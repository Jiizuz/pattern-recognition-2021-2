package com.github.jiizuz.patternrecognition.pattern.filter;

import com.github.jiizuz.patternrecognition.pattern.Pattern;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import lombok.NonNull;

import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link PatternFilter} that takes <tt>X%</tt> random characteristics
 * of the Patterns.
 *
 * @author <a href="mailto:masterchack92@hotmail.com">Jiizuz</a>
 * @see com.github.jiizuz.patternrecognition.pattern.filter.PatternFilter
 * @since 1.0
 */
public class RandomXFilter implements PatternFilter {

    /**
     * X% amount of characteristics to consider.
     */
    private final float x;

    /**
     * {@link Random} utility to generate pseudorandom indexes.
     */
    @NonNull
    private final Random random;

    /**
     * Constructs a new {@link RandomXFilter} with the specified <tt>x</tt>.
     *
     * @param x percentage of characteristics to consider
     * @throws IllegalArgumentException if x <= 0 || x >= 1
     * @throws NullPointerException     if random is <tt>null</tt>
     */
    public RandomXFilter(final float x, final @NonNull Random random) {
        checkArgument(x > 0.0F && x < 1.0F, "x <= 0 || x >= 1", x);
        this.x = x;
        this.random = random;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final @NonNull Pattern pattern) {
        final double[] vector = pattern.getVector();

        pattern.setVector(selectRandomPositions(vector));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Pattern filterCopy(final @NonNull Pattern pattern) {
        final Pattern clone = pattern.clone();

        filter(clone);

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final @NonNull List<Pattern> patterns) {
        final Pattern zero = patterns.get(0);

        final double[] vectorZero = zero.getVector();

        final int amount = Math.max(DoubleMath.roundToInt(vectorZero.length * x, RoundingMode.FLOOR), 1);

        final IntSortedSet indexes = generateRandomIndexes(zero.getVector().length, random, amount);

        for (Pattern pattern : patterns) {
            pattern.setVector(copyFromIndexes(pattern.getVector(), indexes));
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<Pattern> filterCopy(final @NonNull List<Pattern> patterns) {
        final List<Pattern> copy = Lists.newArrayList(patterns);

        // Clone and filter since filterCopy corrupts the vector, and we need symmetry on filter

        copy.replaceAll(Pattern::clone); // deep copy

        filter(copy);

        return copy;
    }

    /**
     * Selects and generates an array with pseudorandom indexes from the
     * specified vector using the current {@link #x} value and retrieving
     * the probabilities from the {@link #random} instance.
     *
     * @param vector to retrieve from the random positions
     * @return a sub-array with the numbers at random-ordered positions from the vector
     */
    private double[] selectRandomPositions(final double[] vector) {
        final int amount = Math.max(DoubleMath.roundToInt(vector.length * x, RoundingMode.FLOOR), 1);

        final IntSortedSet indexes = generateRandomIndexes(vector.length, random, amount);

        return copyFromIndexes(vector, indexes);
    }

    /**
     * Generates a {@link IntSortedSet} with pseudorandom index positions
     * from <tt>0</tt> to the specified <tt>max</tt> index (exclusive) and
     * returns the found indexes.
     *
     * @param max    maximum possible index (exclusive)
     * @param random to generate the pseudorandom probabilities
     * @return the selected random indexes
     */
    @NonNull
    private IntSortedSet generateRandomIndexes(final int max, final Random random, final int amount) {
        checkArgument(amount <= max, "required amount > maximum index");

        final IntSortedSet indexes = new IntAVLTreeSet();

        do {
            indexes.add(random.nextInt(max));
        } while (indexes.size() < amount);

        return indexes;
    }

    /**
     * Adjust the specified src array into another array selecting only
     * the indexes at the specified Set.
     *
     * @param src     to read from the values
     * @param indexes of the src to copy into a fit array
     */
    private double @NonNull [] copyFromIndexes(final double[] src, final @NonNull IntSortedSet indexes) {
        final double[] copy = new double[indexes.size()];

        int i = 0;
        final IntIterator iterator = indexes.intIterator();
        while (iterator.hasNext()) {
            copy[i++] = src[iterator.nextInt()];
        }

        return copy;
    }
}
