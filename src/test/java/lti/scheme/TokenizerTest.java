package lti.scheme;

import static org.junit.jupiter.api.Assertions.*;

import lti.scheme.Token.*;
import org.junit.jupiter.api.Test;

class TokenizerTest {

  @Test
  void tokenizeAtom() {
    var tokenizer = new Tokenizer("hello");
    assertEquals(new Atom("hello"), tokenizer.nextToken());
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeList() {
    var tokenizer = new Tokenizer("(a b)");
    assertEquals(new LeftParen(), tokenizer.nextToken());
    assertEquals(new Atom("a"), tokenizer.nextToken());
    assertEquals(new Atom("b"), tokenizer.nextToken());
    assertEquals(new RightParen(), tokenizer.nextToken());
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeBoolean() {
    var tokenizer = new Tokenizer("#t #f");
    assertEquals(new Atom("#t"), tokenizer.nextToken());
    assertEquals(new Atom("#f"), tokenizer.nextToken());
  }

  @Test
  void tokenizeDot() {
    var tokenizer = new Tokenizer("(a . b)");
    assertEquals(new LeftParen(), tokenizer.nextToken());
    assertEquals(new Atom("a"), tokenizer.nextToken());
    assertEquals(new Dot(), tokenizer.nextToken());
    assertEquals(new Atom("b"), tokenizer.nextToken());
    assertEquals(new RightParen(), tokenizer.nextToken());
  }

  @Test
  void tokenizeDotInSymbol() {
    var tokenizer = new Tokenizer("...");
    assertEquals(new Atom("..."), tokenizer.nextToken());
  }

  @Test
  void tokenizeQuote() {
    var tokenizer = new Tokenizer("'x");
    assertEquals(new Quote(), tokenizer.nextToken());
    assertEquals(new Atom("x"), tokenizer.nextToken());
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeQuotedList() {
    var tokenizer = new Tokenizer("'(a b)");
    assertEquals(new Quote(), tokenizer.nextToken());
    assertEquals(new LeftParen(), tokenizer.nextToken());
    assertEquals(new Atom("a"), tokenizer.nextToken());
    assertEquals(new Atom("b"), tokenizer.nextToken());
    assertEquals(new RightParen(), tokenizer.nextToken());
  }

  @Test
  void tokenizeEmptyInput() {
    var tokenizer = new Tokenizer("");
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeWhitespaceOnly() {
    var tokenizer = new Tokenizer("   \t\n  ");
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeNestedLists() {
    var tokenizer = new Tokenizer("((a))");
    assertEquals(new LeftParen(), tokenizer.nextToken());
    assertEquals(new LeftParen(), tokenizer.nextToken());
    assertEquals(new Atom("a"), tokenizer.nextToken());
    assertEquals(new RightParen(), tokenizer.nextToken());
    assertEquals(new RightParen(), tokenizer.nextToken());
    assertEquals(new Eof(), tokenizer.nextToken());
  }

  @Test
  void tokenizeDotFollowedByDigit() {
    var tokenizer = new Tokenizer(".5");
    assertEquals(new Atom(".5"), tokenizer.nextToken());
  }
}
