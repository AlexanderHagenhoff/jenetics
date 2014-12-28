/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics.util;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jenetics.internal.collection.ArrayProxyMSeq;
import org.jenetics.internal.collection.ObjectArrayProxy;

/**
 * Mutable, ordered, fixed sized sequence.
 *
 * @see ISeq
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 3.0 &mdash; <em>$Date: 2014-12-12 $</em>
 */
public interface MSeq<T> extends Seq<T>, Copyable<MSeq<T>> {

	/**
	 * Set the {@code value} at the given {@code index}.
	 *
	 * @param index the index of the new value.
	 * @param value the new value.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         <code>(index &lt; 0 || index &gt;= size())</code>.
	 */
	public void set(final int index, final T value);

	/**
	 * Fills the sequence with values of the given iterator.
	 *
	 * @param it the iterator of the values to fill this sequence.
	 * @return {@code this} sequence.
	 */
	public default MSeq<T> setAll(final Iterator<? extends T> it) {
		for (int i = 0, n = length(); i < n && it.hasNext(); ++i) {
			set(i, it.next());
		}
		return this;
	}

	/**
	 * Fills the sequence with values of the given iterable.
	 *
	 * @param values the values to fill this sequence.
	 * @return {@code this} sequence.
	 */
	public default MSeq<T> setAll(final Iterable<? extends T> values) {
		setAll(values.iterator());
		return this;
	}

	/**
	 * Fill the sequence with the given values.
	 *
	 * @param values the first initial values of this sequence
	 * @return {@code this} sequence.
	 */
	public default MSeq<T> setAll(final T[] values) {
		for (int i = 0, n = length(); i < n && i < values.length; ++i) {
			set(i, values[i]);
		}
		return this;
	}

	/**
	 * Fill the sequence with values generated by the given factory.
	 *
	 * @param supplier the value factory.
	 * @return {@code this} sequence.
	 * @throws NullPointerException if the given {@code factory} is {@code null}.
	 */
	public default MSeq<T> fill(final Supplier<? extends T> supplier) {
		for (int i = 0, n = length(); i < n; ++i) {
			set(i, supplier.get());
		}
		return this;
	}

	/**
	 * Swap the elements at the two positions.
	 *
	 * @param i the index of the first element.
	 * @param j the index of the second element.
	 * @throws IndexOutOfBoundsException if {@code i < 0 || j >= length()}.
	 */
	public default void swap(final int i, final int j) {
		final T temp = get(i);
		set(i, get(j));
		set(j, temp);
	}

	/**
	 * Swap a given range with a range of the same size with another array.
	 *
	 * <pre>
	 *            start                end
	 *              |                   |
	 * this:  +---+---+---+---+---+---+---+---+---+---+---+---+
	 *              +---------------+
	 *                          +---------------+
	 * other: +---+---+---+---+---+---+---+---+---+---+---+---+
	 *                          |
	 *                      otherStart
	 * </pre>
	 *
	 * @param start the start index of {@code this} range, inclusively.
	 * @param end the end index of {@code this} range, exclusively.
	 * @param other the other array to swap the elements with.
	 * @param otherStart the start index of the {@code other} array.
	 * @throws IndexOutOfBoundsException if {@code start > end} or
	 *         if {@code start < 0 || end >= this.length() || otherStart < 0 ||
	 *         otherStart + (end - start) >= other.length()}
	 */
	public default void swap(
		final int start, final int end,
		final MSeq<T> other, final int otherStart
	) {
		if (otherStart < 0 || (otherStart + (end - start)) > length()) {
			throw new ArrayIndexOutOfBoundsException(format(
				"Invalid index range: [%d, %d)",
				otherStart, (otherStart + (end - start))
			));
		}

		if (start < end) {
			for (int i = (end - start); --i >= 0;) {
				final T temp = get(start + i);
				set(start + i, other.get(otherStart + i));
				other.set(otherStart + i, temp);
			}
		}
	}

	/**
	 * Randomize the {@code array} using the {@link Random} object currently
	 * registered in the {@link RandomRegistry} class. The used shuffling
	 * algorithm is from D. Knuth TAOCP, Seminumerical Algorithms, Third edition,
	 * page 142, Algorithm S (Selection sampling technique).
	 *
	 * @return this shuffled sequence
	 */
	public default MSeq<T> shuffle() {
		return shuffle(RandomRegistry.getRandom());
	}

	/**
	 * Randomize the {@code array} using the given {@link Random} object. The used
	 * shuffling algorithm is from D. Knuth TAOCP, Seminumerical Algorithms,
	 * Third edition, page 142, Algorithm S (Selection sampling technique).
	 *
	 * @param random the {@link Random} object to use for randomize.
	 * @return this shuffled sequence
	 * @throws NullPointerException if the random object is {@code null}.
	 */
	public default MSeq<T> shuffle(final Random random) {
		for (int j = length() - 1; j > 0; --j) {
			swap(j, random.nextInt(j + 1));
		}
		return this;
	}

	/**
	 * Returns a list iterator over the elements in this sequence (in proper
	 * sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper
	 *         sequence)
	 */
	public ListIterator<T> listIterator();

	@Override
	public MSeq<T> subSeq(final int start, final int end);

	@Override
	public MSeq<T> subSeq(final int start);

	@Override
	public <B> MSeq<B> map(final Function<? super T, ? extends B> mapper);

	/**
	 * Return a read-only projection of this sequence. Changes to the original
	 * sequence will not influence the returned {@code ISeq}.
	 *
	 * @return a read-only projection of this sequence
	 */
	public ISeq<T> toISeq();


	/* *************************************************************************
	 *  Some static factory methods.
	 * ************************************************************************/

	/**
	 * Returns a {@code Collector} that accumulates the input elements into a
	 * new {@code MSeq}.
	 *
	 * @param <T> the type of the input elements
	 * @return a {@code Collector} which collects all the input elements into a
	 *         {@code MSeq}, in encounter order
	 */
	public static <T> Collector<T, ?, MSeq<T>> toMSeq() {
		return Collector.of(
			(Supplier<List<T>>)ArrayList::new,
			List::add,
			(left, right) -> { left.addAll(right); return left; },
			MSeq::of
		);
	}

	/**
	 * Single instance of an empty {@code MSeq}.
	 */
	public static final MSeq<?> EMPTY = ofLength(0);

	/**
	 * Return an empty {@code MSeq}.
	 *
	 * @param <T> the element type of the new {@code MSeq}.
	 * @return an empty {@code MSeq}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> MSeq<T> empty() {
		return (MSeq<T>)EMPTY;
	}

	/**
	 * Create a new {@code MSeq} with the given {@code length}.
	 *
	 * @param length the length of the created {@code MSeq}.
	 * @param <T> the element type of the new {@code MSeq}.
	 * @return the new mutable sequence.
	 */
	public static <T> MSeq<T> ofLength(final int length) {
		return new ArrayProxyMSeq<>(new ObjectArrayProxy<T>(length));
	}

	/**
	 * Create a new {@code MSeq} from the given values.
	 *
	 * @param <T> the element type
	 * @param values the array values.
	 * @return a new {@code Meq} with the given values.
	 * @throws NullPointerException if the {@code values} array is {@code null}.
	 */
	@SafeVarargs
	public static <T> MSeq<T> of(final T... values) {
		final ObjectArrayProxy<T> proxy = new ObjectArrayProxy<>(
			values.clone(), 0, values.length
		);
		return new ArrayProxyMSeq<>(proxy);
	}

	/**
	 * Create a new {@code MSeq} from the given values.
	 *
	 * @param <T> the element type
	 * @param values the array values.
	 * @return a new {@code MSeq} with the given values.
	 * @throws NullPointerException if the {@code values} array is {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> MSeq<T> of(final Iterable<? extends T> values) {
		MSeq<T> mseq = null;
		if (values instanceof ISeq<?>) {
			mseq = ((ISeq<T>)values).copy();
		} else if (values instanceof MSeq<?>) {
			mseq = (MSeq<T>)values;
		} else if (values instanceof Collection<?>) {
			final Collection<T> collection = (Collection<T>)values;
			mseq = MSeq.<T>ofLength(collection.size()).setAll(values);
		} else {
			int length = 0;
			for (final T value : values) ++length;

			mseq = MSeq.ofLength(length);
			int index = 0;
			for (final T value : values) mseq.set(index++, value);
		}

		return mseq;
	}

	/**
	 * Create a new {@code MSeq} from the values of the given {@code Seq}.
	 *
	 * @param <T> the element type
	 * @param values the array values.
	 * @return an new {@code MSeq} with the given values
	 * @throws NullPointerException if the {@code values} array is {@code null}.
	 */
	public static <T> MSeq<T> of(final Seq<T> values) {
		return values instanceof ArrayProxyMSeq<?, ?> ?
			((ArrayProxyMSeq<T, ?>)values).copy() :
			MSeq.<T>ofLength(values.length()).setAll(values);
	}

}
