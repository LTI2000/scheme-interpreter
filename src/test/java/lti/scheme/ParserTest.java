package lti.scheme;

import static org.junit.jupiter.api.Assertions.*;

import lti.scheme.Value.*;
import lti.scheme.parser.Parser;
import lti.scheme.parser.ParserException;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  void parseSymbol() {
    assertEquals(new Symbol("x"), Parser.parse("x"));
  }

  @Test
  void parseBoolean() {
    assertEquals(new Bool(true), Parser.parse("#t"));
    assertEquals(new Bool(false), Parser.parse("#f"));
  }

  @Test
  void parseEmptyList() {
    assertEquals(new Nil(), Parser.parse("()"));
  }

  @Test
  void parseList() {
    assertEquals(
        new Pair(new Symbol("a"), new Pair(new Symbol("b"), new Nil())), Parser.parse("(a b)"));
  }

  @Test
  void parseDottedPair() {
    assertEquals(new Pair(new Symbol("a"), new Symbol("b")), Parser.parse("(a . b)"));
  }

  @Test
  void parseImproperList() {
    assertEquals(
        new Pair(new Symbol("a"), new Pair(new Symbol("b"), new Symbol("c"))),
        Parser.parse("(a b . c)"));
  }

  @Test
  void parseQuotedSymbol() {
    // 'x  →  (quote x)
    assertEquals(
        new Pair(new Symbol("quote"), new Pair(new Symbol("x"), new Nil())), Parser.parse("'x"));
  }

  @Test
  void parseQuotedList() {
    // '(a b)  →  (quote (a b))
    var inner = new Pair(new Symbol("a"), new Pair(new Symbol("b"), new Nil()));
    assertEquals(new Pair(new Symbol("quote"), new Pair(inner, new Nil())), Parser.parse("'(a b)"));
  }

  @Test
  void parseNestedQuotes() {
    // ''x  →  (quote (quote x))
    var innerQuote = new Pair(new Symbol("quote"), new Pair(new Symbol("x"), new Nil()));
    assertEquals(
        new Pair(new Symbol("quote"), new Pair(innerQuote, new Nil())), Parser.parse("''x"));
  }

  @Test
  void parseNestedList() {
    var result = Parser.parse("(a (b c))");
    // Pair(a, Pair(Pair(b, Pair(c, Nil)), Nil))
    assertInstanceOf(Pair.class, result);
    var outer = (Pair) result;
    assertEquals(new Symbol("a"), outer.car());
    assertInstanceOf(Pair.class, outer.cdr());
    var cdr = (Pair) outer.cdr();
    assertInstanceOf(Pair.class, cdr.car());
  }

  @Test
  void unclosedListThrows() {
    assertThrows(ParserException.class, () -> Parser.parse("(a b"));
  }

  @Test
  void invalidDottedPairThrows() {
    // Multiple values after dot
    assertThrows(ParserException.class, () -> Parser.parse("(a . b c)"));
  }

  @Test
  void unexpectedRightParenThrows() {
    assertThrows(ParserException.class, () -> Parser.parse(")"));
  }

  @Test
  void unexpectedDotThrows() {
    assertThrows(ParserException.class, () -> Parser.parse("."));
  }

  @Test
  void parseComplexExpression() {
    // (lambda (x) (if #t x #f))
    var result = Parser.parse("(lambda (x) (if #t x #f))");
    assertInstanceOf(Pair.class, result);
    var list = (Pair) result;
    assertEquals(new Symbol("lambda"), list.car());
  }

  @Test
  void parseDottedPairAtListStart() {
    // (. a) is invalid - dot at start
    assertThrows(ParserException.class, () -> Parser.parse("(. a)"));
  }
}
