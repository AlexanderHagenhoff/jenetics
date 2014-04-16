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

/**
 * Interface for accumulating values of a given type. Here an usage example:
 *
 * [code]
 * final MinMax&lt;Double&gt; minMax = new MinMax&lt;&gt;();
 * final Variance&lt;Double&gt; variance = new Variance&lt;&gt;();
 * final Quantile&lt;Double&gt; quantile = new Quantile&lt;&gt;();
 *
 * final List&lt;Double&gt; values = ...;
 * accumulators.accumulate(values, minMax, variance, quantile);
 * [/code]
 *
 * @see accumulators
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 2.0 &mdash; <em>$Date: 2014-03-28 $</em>
 */
public interface Accumulator<T> {

	/**
	 * Accumulate the given value.
	 *
	 * @param value the value to accumulate.
	 */
	public void accumulate(final T value);

}
