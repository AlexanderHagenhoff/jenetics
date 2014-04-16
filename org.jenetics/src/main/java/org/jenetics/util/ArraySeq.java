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
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 2.0 &mdash; <em>$Date: 2014-03-10 $</em>
 */
abstract class ArraySeq<T> implements Seq<T>, Serializable {
	private static final long serialVersionUID = 1L;

	transient ArrayRef _array;
	transient int _start;
	transient int _end;
	transient int _length;

	/**
	 * <i>Universal</i> array constructor.
	 *
	 * @param array the array which holds the elements. The array will not be
	 *         copied.
	 * @param start the start index of the given array (exclusively).
	 * @param end the end index of the given array (exclusively)
	 * @throws NullPointerException if the given {@code array} is {@code null}.
	 * @throws IndexOutOfBoundsException for an illegal start/end point index
	 *          value ({@code start < 0 || end > array.length || start > end}).
	 */
	ArraySeq(final ArrayRef array, final int start, final int end) {
		requireNonNull(array, "Array");
		if (start < 0 || end > array.length || start > end) {
			throw new ArrayIndexOutOfBoundsException(format(
				"Invalid index range: [%d, %s)", start, end
			));
		}
		_array = array;
		_start = start;
		_end = end;
		_length = _end - _start;
	}

	ArraySeq(final int length) {
		this(new ArrayRef(length), 0, length);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(final int index) {
		checkIndex(index);
		return (T)_array.data[index + _start];
	}

	@Override
	public int indexOf(final Object element) {
		return indexOf(element, 0, length());
	}

	@Override
	public int indexOf(final Object element, final int start) {
		return indexOf(element, start, length());
	}

	@Override
	public int indexOf(final Object element, final int start, final int end) {
		checkIndex(start, end);

		final int n = end + _start;
		int index = -1;
		if (element == null) {
			for (int i = start + _start; i < n && index == -1; ++i) {
				if (_array.data[i] == null) {
					index = i - _start;
				}
			}
		} else {
			for (int i = _start + start; i < n && index == -1; ++i) {
				if (element.equals(_array.data[i])) {
					index = i - _start;
				}
			}
		}

		return index;
	}

	@Override
	public int indexWhere(final Function<? super T, Boolean> predicate) {
		return indexWhere(predicate, 0, length());
	}

	@Override
	public int indexWhere(
		final Function<? super T, Boolean> predicate,
		final int start
	) {
		return indexWhere(predicate, start, length());
	}

	@Override
	public int indexWhere(
		final Function<? super T, Boolean> predicate,
		final int start,
		final int end
	) {
		requireNonNull(predicate, "Predicate");

		int index = -1;

		for (int i = start + _start, n = end + _start; i < n && index == -1; ++i) {
			@SuppressWarnings("unchecked")
			final T element = (T)_array.data[i];

			if (predicate.apply(element) == Boolean.TRUE) {
				index = i - _start;
			}
		}

		return index;
	}

	@Override
	public int lastIndexOf(final Object element) {
		return lastIndexOf(element, 0, length());
	}

	@Override
	public int lastIndexOf(final Object element, final int end) {
		return lastIndexOf(element, 0, end);
	}

	@Override
	public int lastIndexOf(final Object element, final int start, final int end) {
		checkIndex(start, end);

		int index = -1;

		if (element == null) {
			for (int i = end + _start; --i >= start + _start && index == -1;) {
				if (_array.data[i] == null) {
					index = i - _start;
				}
			}
		} else {
			for (int i = end + _start; --i >= start + _start && index == -1;) {
				if (element.equals(_array.data[i])) {
					index = i - _start;
				}
			}
		}

		return index;
	}

	@Override
	public int lastIndexWhere(final Function<? super T, Boolean> predicate) {
		return lastIndexWhere(predicate, 0, length());
	}

	@Override
	public int lastIndexWhere(
		final Function<? super T, Boolean> predicate,
		final int end
	) {
		return lastIndexWhere(predicate, 0, end);
	}

	@Override
	public int lastIndexWhere(
		final Function<? super T, Boolean> predicate,
		final int start,
		final int end
	) {
		requireNonNull(predicate, "Predicate");
		checkIndex(start, end);

		int index = -1;

		for (int i = end + _start; --i >= start +_start && index == -1;) {
			@SuppressWarnings("unchecked")
			final T element = (T)_array.data[i];
			if (predicate.apply(element) == Boolean.TRUE) {
				index = i - _start;
			}
		}

		return index;
	}

	@Override
	public <R> void forEach(final Function<? super T, ? extends R> function) {
		requireNonNull(function, "Function");

		for (int i = _start; i < _end; ++i) {
			@SuppressWarnings("unchecked")
			final T element = (T)_array.data[i];
			function.apply(element);
		}
	}

	@Override
	public boolean forAll(final Function<? super T, Boolean> predicate) {
		requireNonNull(predicate, "Predicate");

		boolean valid = true;
		for (int i = _start; i < _end && valid; ++i) {
			@SuppressWarnings("unchecked")
			final T element = (T)_array.data[i];
			valid = predicate.apply(element);
		}
		return valid;
	}

	/*
	<B> B foldLeft(final B z, final Function2<? super B, ? super T, ? extends B> op) {
		B result = z;
		for (int i = 0, n = length(); i < n; ++i) {
			@SuppressWarnings("unchecked")
			final T value = (T)_array.data[i + _start];
			result = op.apply(result, value);
		}
		return z;
	}

	<B> B foldRight(final B z, final Function2<? super T, ? super B, ? extends B> op) {
		B result = z;
		for (int i = length(); --i >= 0;) {
			@SuppressWarnings("unchecked")
			final T value = (T)_array.data[i + _start];
			result = op.apply(value, result);
		}
		return z;
	}

	interface Function2<T1, T2, R> {
		R apply(T1 t1, T2 t2);
	}
	*/

	@Override
	public boolean contains(final Object element) {
		return indexOf(element) != -1;
	}

	@Override
	public int length() {
		return _length;
	}

	@Override
	public Iterator<T> iterator() {
		return new ArraySeqIterator<>(this);
	}

	@Override
	public <B> Iterator<B> iterator(
		final Function<? super T, ? extends B> converter
	) {
		requireNonNull(converter, "Converter");

		return new Iterator<B>() {
			private final Iterator<T> _iterator = iterator();
			@Override public boolean hasNext() {
				return _iterator.hasNext();
			}
			@Override public B next() {
				return converter.apply(_iterator.next());
			}
			@Override public void remove() {
				_iterator.remove();
			}
		};
	}

	@Override
	public Object[] toArray() {
		Object[] array = null;
		if (length() == _array.data.length) {
			array = _array.data.clone();
		} else {
			array = new Object[length()];
			arraycopy(_array.data, _start, array, 0, length());
		}

		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray(final T[] array) {
		T[] result = null;
		if (array.length < length()) {
			result = (T[])copyOfRange(
				_array.data, _start, _end, array.getClass()
			);
		} else {
			arraycopy(_array.data, _start, array, 0, length());
			if (array.length > length()) {
				array[length()] = null;
			}
			result = array;
		}

		return result;
	}

	@Override
	public List<T> asList() {
		return new ArraySeqList<>(this);
	}

	final void checkIndex(final int index) {
		if (index < 0 || index >= _length) {
			throw new ArrayIndexOutOfBoundsException(format(
				"Index %s is out of bounds [0, %s)", index, length()
			));
		}
	}

	final void checkIndex(final int from, final int to) {
		if (from > to) {
			throw new ArrayIndexOutOfBoundsException(
				"fromIndex(" + from + ") > toIndex(" + to+ ")"
			);
		}
		if (from < 0 || to > _length) {
			throw new ArrayIndexOutOfBoundsException(format(
				"Invalid index range: [%d, %s)", from, to
			));
		}
	}

	@Override
	public int hashCode() {
		return arrays.hashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this ||
				obj instanceof ArraySeq<?> && arrays.equals(this, obj);
	}

	@Override
	public String toString(
		final String prefix,
		final String separator,
		final String suffix
	) {
		  final StringBuilder out = new StringBuilder();

		  out.append(prefix);
		  if (length() > 0) {
			out.append(_array.data[_start]);
		  }
		  for (int i = _start + 1; i < _end; ++i) {
			out.append(separator);
			out.append(_array.data[i]);
		  }
		  out.append(suffix);

		  return out.toString();
	}

	@Override
	public String toString(final String separator) {
		return toString("", separator, "");
	}

	@Override
	public String toString() {
		  return toString("[", ",", "]");
	}

	private void writeObject(final ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();

		out.writeInt(length());
		for (int i = _start; i < _end; ++i) {
			out.writeObject(_array.data[i]);
		}
	}

	private void readObject(final ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		_length = in.readInt();
		_array = new ArrayRef(_length);
		_start = 0;
		_end = _length;
		for (int i = 0; i < _length; ++i) {
			_array.data[i] = in.readObject();
		}
	}

}
