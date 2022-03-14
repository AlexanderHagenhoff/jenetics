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
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.ext.grammar;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.engine.Codec;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;

import io.jenetics.ext.grammar.Cfg.NonTerminal;
import io.jenetics.ext.grammar.Cfg.Symbol;
import io.jenetics.ext.grammar.Cfg.Terminal;

/**
 * This class contains low-level methods for creating <em>sentences</em> from a
 * given context-free grammar ({@link Cfg}). A sentence is defined as list of
 * {@link Terminal}s, {@code List<Cfg.Terminal>}.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
public final class GrammarCodecs {
	private GrammarCodecs() {}

	public static String toString(final List<? extends Symbol<String>> symbols) {
		return symbols.stream()
			.map(symbol -> symbol instanceof NonTerminal<?> nt
				? "<%s>".formatted(nt)
				: symbol.name())
			.collect(Collectors.joining());
	}

	public static <T> List<Terminal<T>> generate(
		final Cfg<T> cfg,
		final SymbolIndex index,
		final int limit
	) {
		return SentenceGenerator.<T>of(index, limit).generate(cfg);
	}

	/* *************************************************************************
	 * Codec factories
	 * ************************************************************************/

	/**
	 * The returned encoding uses a separate <em>codons</em>, backed up by a
	 * {@link IntegerChromosome}, for every rule. The length of the chromosome
	 * equal to the number of <em>alternative</em> expressions of the rule. This
	 * means that the following CFG,
	 *
	 * <pre>{@code
	 *                       (0)            (1)
	 * (0) <expr> ::= (<expr><op><expr>) | <var>
	 *               (0) (1) (2) (3)
	 * (1) <op>   ::= + | - | * | /
	 *               (0) (1) (2) (3) (4)
	 * (2) <var>  ::= x | 1 | 2 | 3 | 4
	 * }</pre>
	 *
	 * will be represented by the following {@link Genotype}
	 * <pre>{@code
	 * Genotype.of(
	 *     IntegerChromosome.of(IntRange.of(0, 2), length.applyAsInt(2)),
	 *     IntegerChromosome.of(IntRange.of(0, 4), length.applyAsInt(4)),
	 *     IntegerChromosome.of(IntRange.of(0, 5), length.applyAsInt(5))
	 * )
	 * }</pre>
	 *
	 * The {@code length} function lets you defining the number of codons as
	 * function of the number of alternatives for a given rule.
	 *
	 * <pre>{@code
	 * final Cfg<String> cfg = Bnf.parse(...);
	 * final Codec<List<Terminal<String>> codec = GrammarCodecs.of(
	 *     cfg,
	 *     // The chromosome length is 10 times the
	 *     // number of rule alternatives.
	 *     alternatives -> alternatives*10,
	 *     // Using the standard sentence generator
	 *     // with a maximal sentence length of 1,000.
	 *     index -> new StandardSentenceGenerator(index, 1_000)
	 * );
	 * }</pre>
	 *
	 * @param cfg grammar
	 * @param length the length of the chromosome which is used for selecting
	 *        rules and symbols. The input parameter for this function is the
	 *        number of alternatives of the actual rule. This way it is possible
	 *        to define the chromosome length dependent on the selectable
	 *        alternatives.
	 * @param generator sentence generator function from a given
	 *        {@link SymbolIndex}
	 * @return a new sentence codec
	 */
	public static <T> Codec<List<Terminal<T>>, IntegerGene> of(
		final Cfg<? extends T> cfg,
		final IntUnaryOperator length,
		final Function<? super SymbolIndex, SentenceGenerator<T>> generator
	) {
		// Every rule gets its own codons. The ranges of the chromosomes will
		// fit exactly the number of rule alternatives.
		final ISeq<IntegerChromosome> chromosomes = cfg.rules().stream()
			.map(rule ->
				IntegerChromosome.of(
					IntRange.of(0, rule.alternatives().size()),
					length.applyAsInt(rule.alternatives().size())
				))
			.collect(ISeq.toISeq());

		final Map<NonTerminal<? extends T>, Integer> ruleIndex = IntStream
			.range(0, cfg.rules().size())
			.mapToObj(i -> Map.entry(cfg.rules().get(i).start(), i))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Function<Genotype<IntegerGene>, SymbolIndex> symbolIndex = gt -> {
			final List<Codons> codons = gt.stream()
				.map(Codons::ofIntegerGenes)
				.toList();

			return (rule, bound) -> codons
				.get(ruleIndex.get(rule.start()))
				.next(rule, bound);
		};

		return Codec.of(
			Genotype.of(chromosomes),
			gt -> generator.apply(symbolIndex.apply(gt)).generate(cfg)
		);
	}











	/**
	 * Codec for sentences, generated by a grammar. Classic encoding via
	 * {@link IntegerChromosome}.
	 *
	 * @param cfg the creating grammar
	 * @param codonRange the value range of the <em>codons</em> used for the
	 *        sentence generation
	 * @param codonCount the length of the chromosomes
	 * @param maxSentenceLength the maximal number of symbols
	 * @return sentence codec
	 */
	public static Codec<List<Terminal<String>>, IntegerGene> codec(
		final Cfg<String> cfg,
		final IntRange codonRange,
		final IntRange codonCount,
		final int maxSentenceLength
	) {
		return Codec.of(
			Genotype.of(IntegerChromosome.of(codonRange, codonCount)),
			gt -> generate(
				cfg,
				Codons.ofIntegerGenes(gt.chromosome()),
				maxSentenceLength
			)
		);
	}

	/**
	 * Codec for sentences, generated by a grammar. Classic encoding via
	 * {@link IntegerChromosome}.
	 *
	 * @param cfg the creating grammar
	 * @param codonRange the value range of the <em>codons</em> used for the
	 *        sentence generation
	 * @param codonCount the length of the chromosomes
	 * @param maxSentenceLength the maximal number of symbols
	 * @return sentence codec
	 */
	public static Codec<List<Terminal<String>>, IntegerGene> codec(
		final Cfg<String> cfg,
		final IntRange codonRange,
		final int codonCount,
		final int maxSentenceLength
	) {
		return Codec.of(
			Genotype.of(IntegerChromosome.of(codonRange, codonCount)),
			gt -> generate(
				cfg,
				Codons.ofIntegerGenes(gt.chromosome()),
				maxSentenceLength
			)
		);
	}

	public static Codec<List<Terminal<String>>, BitGene> codec(
		final Cfg<String> cfg,
		final int codonCount,
		final int maxSentenceLength
	) {
		return Codec.of(
			Genotype.of(BitChromosome.of(codonCount)),
			gt -> generate(
				cfg,
				Codons.ofBitGenes(gt.chromosome()),
				maxSentenceLength
			)
		);
	}



	public static <T> Codec<List<Terminal<T>>, IntegerGene> codec(
		final Cfg<T> cfg,
		final IntUnaryOperator length,
		final int maxSentenceLength
	) {
		return of(
			cfg, length, index -> SentenceGenerator.of(index, maxSentenceLength)
		);
	}

}
