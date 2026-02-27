package lti.scheme.parser;

import lti.scheme.parser.Token.BooleanLiteral;
import lti.scheme.parser.Token.Eof;
import lti.scheme.parser.Token.LeftParen;
import lti.scheme.parser.Token.NumberLiteral;
import lti.scheme.parser.Token.Quote;
import lti.scheme.parser.Token.RightParen;
import lti.scheme.parser.Token.StringLiteral;
import lti.scheme.parser.Token.Symbol;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenizerTest {
  @Test
  void tokensReturnsSymbolsFromLines() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "foo bar"), new NumberedLine(2, "baz"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new Symbol(1, 1, "foo"), result.get(0));
    assertEquals(new Symbol(1, 5, "bar"), result.get(1));
    assertEquals(new Symbol(2, 1, "baz"), result.get(2));
  }

  @Test
  void tokensHandlesMultipleWhitespace() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "  foo   bar  "));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(1, 3, "foo"), result.get(0));
    assertEquals(new Symbol(1, 9, "bar"), result.get(1));
  }

  @Test
  void tokensIgnoresComments() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "foo ; this is a comment"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new Symbol(1, 1, "foo"), result.get(0));
  }

  @Test
  void tokensIgnoresFullLineComments() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "; comment line"), new NumberedLine(2, "actual code"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(2, 1, "actual"), result.get(0));
    assertEquals(new Symbol(2, 8, "code"), result.get(1));
  }

  @Test
  void tokensReturnsEmptyStreamForEmptyInput() {
    Stream<NumberedLine> input = Stream.empty();
    List<Token> result = Tokenizer.tokens(input).toList();

    assertTrue(result.isEmpty());
  }

  @Test
  void tokensReturnsEmptyStreamForBlankLines() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "   "), new NumberedLine(2, "\t\t"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertTrue(result.isEmpty());
  }

  @Test
  void tokensHandlesTabsAsWhitespace() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "foo\tbar\tbaz"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new Symbol(1, 1, "foo"), result.get(0));
    assertEquals(new Symbol(1, 5, "bar"), result.get(1));
    assertEquals(new Symbol(1, 9, "baz"), result.get(2));
  }

  @Test
  void tokenLineNumberAccessor() {
    Token token = new Symbol(5, 3, "symbol");

    assertEquals(5, token.lineNumber());
  }

  @Test
  void tokenColumnNumberAccessor() {
    Token token = new Symbol(5, 3, "symbol");

    assertEquals(3, token.columnNumber());
  }

  @Test
  void tokensIntegrationWithNumberedLines() {
    BufferedReader reader = new BufferedReader(new StringReader("define x ; comment\n10"));

    try (Stream<String> lines = Streams.lines(reader)) {
      List<Token> result = Tokenizer.tokens(Streams.numberedLines(lines)).toList();
      assertEquals(4, result.size());
      assertEquals(new Symbol(1, 1, "define"), result.get(0));
      assertEquals(new Symbol(1, 8, "x"), result.get(1));
      assertEquals(new NumberLiteral(2, 1, 10), result.get(2));
      assertEquals(new Eof(3, 1), result.get(3));
    }
  }
  // S-expression specific tests

  @Test
  void tokensRecognizesParentheses() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "(define x 10)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1, 1), result.get(0));
    assertEquals(new Symbol(1, 2, "define"), result.get(1));
    assertEquals(new Symbol(1, 9, "x"), result.get(2));
    assertEquals(new NumberLiteral(1, 11, 10), result.get(3));
    assertEquals(new RightParen(1, 13), result.get(4));
  }

  @Test
  void tokensRecognizesNestedParentheses() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "((a))"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1, 1), result.get(0));
    assertEquals(new LeftParen(1, 2), result.get(1));
    assertEquals(new Symbol(1, 3, "a"), result.get(2));
    assertEquals(new RightParen(1, 4), result.get(3));
    assertEquals(new RightParen(1, 5), result.get(4));
  }

  @Test
  void tokensRecognizesQuote() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "'x"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Quote(1, 1), result.get(0));
    assertEquals(new Symbol(1, 2, "x"), result.get(1));
  }

  @Test
  void tokensRecognizesQuotedList() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "'(a b c)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new Quote(1, 1), result.get(0));
    assertEquals(new LeftParen(1, 2), result.get(1));
    assertEquals(new Symbol(1, 3, "a"), result.get(2));
    assertEquals(new Symbol(1, 5, "b"), result.get(3));
    assertEquals(new Symbol(1, 7, "c"), result.get(4));
    assertEquals(new RightParen(1, 8), result.get(5));
  }

  @Test
  void tokensRecognizesNumbers() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "42 -17 0"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new NumberLiteral(1, 1, 42), result.get(0));
    assertEquals(new NumberLiteral(1, 4, -17), result.get(1));
    assertEquals(new NumberLiteral(1, 8, 0), result.get(2));
  }

  @Test
  void tokensRecognizesBooleans() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "#t #f"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new BooleanLiteral(1, 1, true), result.get(0));
    assertEquals(new BooleanLiteral(1, 4, false), result.get(1));
  }

  @Test
  void tokensRecognizesStringLiterals() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "\"hello world\""));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new StringLiteral(1, 1, "hello world"), result.get(0));
  }

  @Test
  void tokensHandlesEscapeSequencesInStrings() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "\"line1\\nline2\\ttab\\\"quote\\\\backslash\""));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new StringLiteral(1, 1, "line1\nline2\ttab\"quote\\backslash"), result.get(0));
  }

  @Test
  void tokensRecognizesComplexSExpression() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "(if #t \"yes\" \"no\")"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new LeftParen(1, 1), result.get(0));
    assertEquals(new Symbol(1, 2, "if"), result.get(1));
    assertEquals(new BooleanLiteral(1, 5, true), result.get(2));
    assertEquals(new StringLiteral(1, 8, "yes"), result.get(3));
    assertEquals(new StringLiteral(1, 14, "no"), result.get(4));
    assertEquals(new RightParen(1, 18), result.get(5));
  }

  @Test
  void tokensHandlesParensWithoutWhitespace() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "(add(mul 2 3)4)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(9, result.size());
    assertEquals(new LeftParen(1, 1), result.get(0));
    assertEquals(new Symbol(1, 2, "add"), result.get(1));
    assertEquals(new LeftParen(1, 5), result.get(2));
    assertEquals(new Symbol(1, 6, "mul"), result.get(3));
    assertEquals(new NumberLiteral(1, 10, 2), result.get(4));
    assertEquals(new NumberLiteral(1, 12, 3), result.get(5));
    assertEquals(new RightParen(1, 13), result.get(6));
    assertEquals(new NumberLiteral(1, 14, 4), result.get(7));
    assertEquals(new RightParen(1, 15), result.get(8));
  }

  @Test
  void tokensRecognizesSymbolsWithSpecialChars() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "add+ sub- mul* div/ eq? set!"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new Symbol(1, 1, "add+"), result.get(0));
    assertEquals(new Symbol(1, 6, "sub-"), result.get(1));
    assertEquals(new Symbol(1, 11, "mul*"), result.get(2));
    assertEquals(new Symbol(1, 16, "div/"), result.get(3));
    assertEquals(new Symbol(1, 21, "eq?"), result.get(4));
    assertEquals(new Symbol(1, 25, "set!"), result.get(5));
  }

  @Test
  void tokensHandlesMinusAsSymbol() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "(- 5 3)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1, 1), result.get(0));
    assertEquals(new Symbol(1, 2, "-"), result.get(1));
    assertEquals(new NumberLiteral(1, 4, 5), result.get(2));
    assertEquals(new NumberLiteral(1, 6, 3), result.get(3));
    assertEquals(new RightParen(1, 7), result.get(4));
  }

  @Test
  void tokensHandlesHashSymbolsNotBooleans() {
    Stream<NumberedLine> input = Stream.of(new NumberedLine(1, "#true #false"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(1, 1, "#true"), result.get(0));
    assertEquals(new Symbol(1, 7, "#false"), result.get(1));
  }
}
