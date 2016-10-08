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
package org.jenetics.engine;

import static java.util.Objects.requireNonNull;

import java.util.function.BiPredicate;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;

import org.jenetics.stat.DoubleMomentStatistics;
import org.jenetics.stat.DoubleMoments;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class FitnessConvergenceLimit<N extends Number & Comparable<? super N>>
	implements Predicate<EvolutionResult<?, N>>
{

	private final Buffer _shortBuffer;
	private final Buffer _longBuffer;
	private final BiPredicate<DoubleMoments, DoubleMoments> _limit;

	private long _generation;

	FitnessConvergenceLimit(
		final int shortFilterSize,
		final int longFilterSize,
		final BiPredicate<DoubleMoments, DoubleMoments> limit
	) {
		_shortBuffer = new Buffer(shortFilterSize);
		_longBuffer = new Buffer(longFilterSize);
		_limit = requireNonNull(limit);
	}

	@Override
	public boolean test(final EvolutionResult<?, N> result) {
		final Number fitness = result.getBestFitness();

		if (fitness != null) {
			_shortBuffer.accept(fitness.doubleValue());
			_longBuffer.accept(fitness.doubleValue());
			++_generation;
		}

		return _generation < _longBuffer.length() ||
			_limit.test(_shortBuffer.doubleMoments(), _longBuffer.doubleMoments());
	}

	private static final class Buffer implements DoubleConsumer {
		private final double[] _buffer;

		private int _next;

		Buffer(final int length) {
			_buffer = new double[length];
		}

		@Override
		public void accept(final double value) {
			_buffer[_next++] = value;
			if (_next == _buffer.length) {
				_next = 0;
			}
		}

		public int length() {
			return _buffer.length;
		}

		public DoubleMoments doubleMoments() {
			final DoubleMomentStatistics statistics = new DoubleMomentStatistics();
			for (int i = _buffer.length; --i >=0;) {
				statistics.accept(_buffer[i]);
			}

			return DoubleMoments.of(statistics);
		}
	}

}
