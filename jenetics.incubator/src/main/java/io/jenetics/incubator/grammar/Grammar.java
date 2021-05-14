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
package io.jenetics.incubator.grammar;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a context-free grammar.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
public final class Grammar {

	/**
	 * Represents the <em>symbols</em> the (context-free) grammar consists.
	 */
	public sealed interface Symbol {

		/**
		 * Return the value of the symbol.
		 *
		 * @return the value of the symbol
		 */
		String value();
	}

	/**
	 * Represents the non-terminal symbols of the grammar.
	 *
	 * @param value the value of the non-terminal symbol
	 */
	public static record NonTerminal(String value) implements Symbol {
		@Override
		public String toString() {
			return format("<%s>", value);
		}
	}

	/**
	 * Represents a terminal symbols of the grammar.
	 *
	 * @param value the value of the terminal symbol
	 */
	public static record Terminal(String value) implements Symbol {
		@Override
		public String toString() {
			return BnfParser.escape(value);
		}
	}

	/**
	 * Represents one <em>expression</em> a production rule consists of.
	 */
	public static record Expression(List<Symbol> symbols) {

		/**
		 * @param symbols the list of symbols of the expression
		 * @throws IllegalArgumentException if the list of {@code symbols} is
		 *         empty
		 */
		public Expression {
			if (symbols.isEmpty()) {
				throw new IllegalArgumentException(
					"The list of symbols must not be empty."
				);
			}
			symbols = List.copyOf(symbols);
		}

		@Override
		public String toString() {
			return symbols.stream()
				.map(Object::toString)
				.collect(Collectors.joining(" "));
		}
	}

	/**
	 * Represents a production rule of the grammar.
	 */
	public static record Rule(NonTerminal start, List<Expression> alternatives) {

		/**
		 * Creates a new rule object.
		 *
		 * @param start the start symbol of the rule
		 * @param alternatives the list af expressions of the rules
		 * @throws IllegalArgumentException if the given list of
		 *         {@code alternatives} is empty
		 * @throws NullPointerException if one of the arguments is {@code null}
		 */
		public Rule {
			requireNonNull(start);
			alternatives = List.copyOf(alternatives);
		}

		@Override
		public String toString() {
			return format(
				"%s ::= %s",
				start,
				alternatives.stream()
					.map(Objects::toString)
					.collect(Collectors.joining("\n    | "))
			);
		}
	}

	private final List<NonTerminal> nonTerminals;
	private final List<Terminal> terminals;
	private final NonTerminal start;
	private final List<Rule> rules;

	/**
	 * Create a grammar object with the given rules.
	 *
	 * @param rules the rules the grammar consists of
	 * @throws IllegalArgumentException if the list of rules is empty
	 * @throws NullPointerException if the list of rules is {@code null}
	 */
	public Grammar(final List<Rule> rules) {
		if (rules.isEmpty()) {
			throw new IllegalArgumentException(
				"The given list of rules must not be empty."
			);
		}

		this.nonTerminals = nonTerminals(rules);
		this.terminals = terminals(rules);
		this.start = rules.get(0).start();
		this.rules = List.copyOf(rules);
	}

	private static List<NonTerminal> nonTerminals(final List<Rule> rules) {
		final var nonTerminals = new ArrayList<NonTerminal>();
		final var distinct = new HashSet<NonTerminal>();

		for (var rule : rules) {
			if (!distinct.contains(rule.start())) {
				nonTerminals.add(rule.start());
				distinct.add(rule.start());
			}
			rule.alternatives().stream()
				.flatMap(e -> e.symbols().stream())
				.filter(s -> s instanceof NonTerminal)
				.map(NonTerminal.class::cast)
				.forEach(nt -> {
					if (!distinct.contains(nt)) {
						nonTerminals.add(nt);
						distinct.add(nt);
					}
				});
		}

		return List.copyOf(nonTerminals);
	}

	private static List<Terminal> terminals(final List<Rule> rules) {
		final var terminals = new ArrayList<Terminal>();
		final var distinct = new HashSet<Terminal>();

		for (var rule : rules) {
			rule.alternatives().stream()
				.flatMap(e -> e.symbols().stream())
				.filter(s -> s instanceof Terminal)
				.map(Terminal.class::cast)
				.forEach(nt -> {
					if (!distinct.contains(nt)) {
						terminals.add(nt);
						distinct.add(nt);
					}
				});
		}

		return List.copyOf(terminals);
	}

	/**
	 * Return the non-terminal symbols of {@code this} grammar.
	 *
	 * @return the non-terminal symbols of {@code this} grammar
	 */
	public List<NonTerminal> nonTerminals() {
		return nonTerminals;
	}

	/**
	 * Return the terminal symbols of {@code this} grammar.
	 *
	 * @return the terminal symbols of {@code this} grammar
	 */
	public List<Terminal> terminals() {
		return terminals;
	}

	/**
	 * Return the start symbol of {@code this} grammar.
	 *
	 * @return the start symbol of {@code this} grammar
	 */
	public NonTerminal start() {
		return start;
	}

	/**
	 * Return the <em>production</em> rules of {@code this} grammar.
	 *
	 * @return the <em>production</em> rules of {@code this} grammar
	 */
	public List<Rule> rules() {
		return rules;
	}

	/**
	 * Return the rule for the given {@code start} symbol.
	 *
	 * @param start the start symbol of the rule
	 * @return the rule for the given {@code start} symbol
	 * @throws NullPointerException if the given {@code start} symbol is
	 *         {@code null}
	 */
	public Optional<Rule> rule(final NonTerminal start) {
		requireNonNull(start);
		return rules.stream()
			.filter(rule -> rule.start().equals(start))
			.findFirst();
	}

	public List<Terminal> generate(final Index index) {
		return StandardGenerators.generateList(this, index);
	}

	public static Grammar parse(final String bnf) {
		return BnfParser.parse(bnf);
	}

	@Override
	public String toString() {
		return rules.stream()
			.map(Object::toString)
			.collect(Collectors.joining("\n"));
	}

}
