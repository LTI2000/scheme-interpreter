package lti.scheme.parser;

import lti.scheme.parser.Token.BooleanLiteral;
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
    Stream<Line> input = Stream.of(
            new Line(1, "foo bar"),
            new Line(2, "baz")
    );
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new Symbol(1, "foo"), result.get(0));
    assertEquals(new Symbol(1, "bar"), result.get(1));
    assertEquals(new Symbol(2, "baz"), result.get(2));
  }

  @Test
  void tokensHandlesMultipleWhitespace() {
    Stream<Line> input = Stream.of(new Line(1, "  foo   bar  "));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(1, "foo"), result.get(0));
    assertEquals(new Symbol(1, "bar"), result.get(1));
  }

  @Test
  void tokensIgnoresComments() {
    Stream<Line> input = Stream.of(new Line(1, "foo ; this is a comment"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new Symbol(1, "foo"), result.get(0));
  }

  @Test
  void tokensIgnoresFullLineComments() {
    Stream<Line> input = Stream.of(
            new Line(1, "; comment line"),
            new Line(2, "actual code")
    );
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(2, "actual"), result.get(0));
    assertEquals(new Symbol(2, "code"), result.get(1));
  }

  @Test
  void tokensReturnsEmptyStreamForEmptyInput() {
    Stream<Line> input = Stream.empty();
    List<Token> result = Tokenizer.tokens(input).toList();
    assertTrue(result.isEmpty());
  }

  @Test
  void tokensReturnsEmptyStreamForBlankLines() {
    Stream<Line> input = Stream.of(
            new Line(1, "   "),
            new Line(2, "\t\t")
    );
    List<Token> result = Tokenizer.tokens(input).toList();
    assertTrue(result.isEmpty());
  }

  @Test
  void tokensHandlesTabsAsWhitespace() {
    Stream<Line> input = Stream.of(new Line(1, "foo\tbar\tbaz"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new Symbol(1, "foo"), result.get(0));
    assertEquals(new Symbol(1, "bar"), result.get(1));
    assertEquals(new Symbol(1, "baz"), result.get(2));
  }

  @Test
  void tokenLineNumberAccessor() {
    Token token = new Symbol(5, "symbol");
    assertEquals(5, token.lineNumber());
  }

  @Test
  void tokensIntegrationWithNumberedLines() {
    BufferedReader reader = new BufferedReader(new StringReader("define x ; comment\n10"));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<Token> result = Tokenizer.tokens(Streams.numberedLines(lines)).toList();

      assertEquals(3, result.size());
      assertEquals(new Symbol(1, "define"), result.get(0));
      assertEquals(new Symbol(1, "x"), result.get(1));
      assertEquals(new NumberLiteral(2, 10), result.get(2));
    }
  }

  // S-expression specific tests

  @Test
  void tokensRecognizesParentheses() {
    Stream<Line> input = Stream.of(new Line(1, "(define x 10)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1), result.get(0));
    assertEquals(new Symbol(1, "define"), result.get(1));
    assertEquals(new Symbol(1, "x"), result.get(2));
    assertEquals(new NumberLiteral(1, 10), result.get(3));
    assertEquals(new RightParen(1), result.get(4));
  }

  @Test
  void tokensRecognizesNestedParentheses() {
    Stream<Line> input = Stream.of(new Line(1, "((a))"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1), result.get(0));
    assertEquals(new LeftParen(1), result.get(1));
    assertEquals(new Symbol(1, "a"), result.get(2));
    assertEquals(new RightParen(1), result.get(3));
    assertEquals(new RightParen(1), result.get(4));
  }

  @Test
  void tokensRecognizesQuote() {
    Stream<Line> input = Stream.of(new Line(1, "'x"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Quote(1), result.get(0));
    assertEquals(new Symbol(1, "x"), result.get(1));
  }

  @Test
  void tokensRecognizesQuotedList() {
    Stream<Line> input = Stream.of(new Line(1, "'(a b c)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new Quote(1), result.get(0));
    assertEquals(new LeftParen(1), result.get(1));
    assertEquals(new Symbol(1, "a"), result.get(2));
    assertEquals(new Symbol(1, "b"), result.get(3));
    assertEquals(new Symbol(1, "c"), result.get(4));
    assertEquals(new RightParen(1), result.get(5));
  }

  @Test
  void tokensRecognizesNumbers() {
    Stream<Line> input = Stream.of(new Line(1, "42 -17 0"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(3, result.size());
    assertEquals(new NumberLiteral(1, 42), result.get(0));
    assertEquals(new NumberLiteral(1, -17), result.get(1));
    assertEquals(new NumberLiteral(1, 0), result.get(2));
  }

  @Test
  void tokensRecognizesBooleans() {
    Stream<Line> input = Stream.of(new Line(1, "#t #f"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new BooleanLiteral(1, true), result.get(0));
    assertEquals(new BooleanLiteral(1, false), result.get(1));
  }

  @Test
  void tokensRecognizesStringLiterals() {
    Stream<Line> input = Stream.of(new Line(1, "\"hello world\""));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new StringLiteral(1, "hello world"), result.get(0));
  }

  @Test
  void tokensHandlesEscapeSequencesInStrings() {
    Stream<Line> input = Stream.of(new Line(1, "\"line1\\nline2\\ttab\\\"quote\\\\backslash\""));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(1, result.size());
    assertEquals(new StringLiteral(1, "line1\nline2\ttab\"quote\\backslash"), result.get(0));
  }

  @Test
  void tokensRecognizesComplexSExpression() {
    Stream<Line> input = Stream.of(new Line(1, "(if #t \"yes\" \"no\")"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new LeftParen(1), result.get(0));
    assertEquals(new Symbol(1, "if"), result.get(1));
    assertEquals(new BooleanLiteral(1, true), result.get(2));
    assertEquals(new StringLiteral(1, "yes"), result.get(3));
    assertEquals(new StringLiteral(1, "no"), result.get(4));
    assertEquals(new RightParen(1), result.get(5));
  }

  @Test
  void tokensHandlesParensWithoutWhitespace() {
    Stream<Line> input = Stream.of(new Line(1, "(add(mul 2 3)4)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(9, result.size());
    assertEquals(new LeftParen(1), result.get(0));
    assertEquals(new Symbol(1, "add"), result.get(1));
    assertEquals(new LeftParen(1), result.get(2));
    assertEquals(new Symbol(1, "mul"), result.get(3));
    assertEquals(new NumberLiteral(1, 2), result.get(4));
    assertEquals(new NumberLiteral(1, 3), result.get(5));
    assertEquals(new RightParen(1), result.get(6));
    assertEquals(new NumberLiteral(1, 4), result.get(7));
    assertEquals(new RightParen(1), result.get(8));
  }

  @Test
  void tokensRecognizesSymbolsWithSpecialChars() {
    Stream<Line> input = Stream.of(new Line(1, "add+ sub- mul* div/ eq? set!"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(6, result.size());
    assertEquals(new Symbol(1, "add+"), result.get(0));
    assertEquals(new Symbol(1, "sub-"), result.get(1));
    assertEquals(new Symbol(1, "mul*"), result.get(2));
    assertEquals(new Symbol(1, "div/"), result.get(3));
    assertEquals(new Symbol(1, "eq?"), result.get(4));
    assertEquals(new Symbol(1, "set!"), result.get(5));
  }

  @Test
  void tokensHandlesMinusAsSymbol() {
    Stream<Line> input = Stream.of(new Line(1, "(- 5 3)"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(5, result.size());
    assertEquals(new LeftParen(1), result.get(0));
    assertEquals(new Symbol(1, "-"), result.get(1));
    assertEquals(new NumberLiteral(1, 5), result.get(2));
    assertEquals(new NumberLiteral(1, 3), result.get(3));
    assertEquals(new RightParen(1), result.get(4));
  }

  @Test
  void tokensHandlesHashSymbolsNotBooleans() {
    Stream<Line> input = Stream.of(new Line(1, "#true #false"));
    List<Token> result = Tokenizer.tokens(input).toList();

    assertEquals(2, result.size());
    assertEquals(new Symbol(1, "#true"), result.get(0));
    assertEquals(new Symbol(1, "#false"), result.get(1));
  }
}
