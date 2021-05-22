package io.jenetics.incubator.parser;

import java.util.stream.Stream;

import org.testng.annotations.Test;

public class BnfParserTest {

	private static final String GRAMMAR_STRING =
		"<expr> ::= ( <expr> <op> <expr> ) | <num> | <var> | ( <expr> <op> <expr> )\n" +
		"<op> ::= '+' | - | '*' | /\n" +
		"<var> ::= x | y\n" +
		"<num> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9";

	@Test
	public void tokenize() {
		final var tokenizer = new BnfParser.BnfTokenizer(GRAMMAR_STRING);
		final Stream<Token> tokens = Stream.generate(tokenizer::next)
			.takeWhile(t -> t.type() != Tokenizer.EOF_TYPE);

		tokens
			.map(BnfParser.BnfTokenizer::toString)
			.forEach(System.out::println);
	}

}
