package io.jenetics.incubator.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import io.jenetics.incubator.grammar.Grammar.NonTerminal;
import io.jenetics.incubator.grammar.Grammar.Symbol;
import io.jenetics.incubator.grammar.Grammar.Terminal;

import io.jenetics.ext.util.Tree;
import io.jenetics.ext.util.TreeNode;

/**
 * Implementation of standard mapping rules.
 */
public final class StandardGenerators {
	private StandardGenerators() {
	}

	/**
	 * Returning a list of terminal symbols, generated from the given
	 * {@code grammar}.
	 *
	 * @param grammar the grammar to use for generating the list of terminals
	 * @param index the index function used for selecting specific symbols from
	 *        the grammar by it's index.
	 * @return a list of terminal symbols
	 */
	public static List<Terminal> generateList(
		final Grammar grammar,
		final Index index
	) {
		final NonTerminal start = grammar.start();
		final LinkedList<Symbol> symbols = new LinkedList<>(expand(grammar, start, index));

		boolean expanded = true;
		while (expanded) {
			expanded = false;

			final var it = symbols.listIterator();
			while (it.hasNext()) {
				final var symbol = it.next();

				if (symbol instanceof NonTerminal) {
					it.remove();
					expand(grammar, (NonTerminal)symbol, index).forEach(it::add);
					expanded = true;
				}
			}
		}

		return symbols.stream()
			.map(Grammar.Terminal.class::cast)
			.toList();
	}

	private static List<Symbol> expand(
		final Grammar grammar,
		final NonTerminal symbol,
		Index index
	) {
		final var rule = grammar.rule(symbol);
		return rule
			.map(r -> r.alternatives()
				.get(index.next(r.alternatives().size()))
				.symbols())
			.orElse(List.of(symbol));
	}

	public static Tree<Symbol, ?> generateTree(
		final Grammar grammar,
		final Index index
	) {
		final NonTerminal start = grammar.start();
		final TreeNode<Symbol> symbols = TreeNode.of(start);

		boolean expanded = true;
		while (expanded) {
			final Optional<TreeNode<Symbol>> tree = symbols.leaves()
				.filter(leaf -> leaf.value() instanceof NonTerminal)
				.filter(leaf -> grammar.rule((NonTerminal)leaf.value()).isPresent())
				.findFirst();

			tree.ifPresent(t ->
				expand(grammar, (NonTerminal)t.value(), index)
					.forEach(t::attach)
			);

			expanded = tree.isPresent();
		}

		return symbols;
	}

}
