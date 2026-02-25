package lti.scheme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import lti.scheme.parser.Token.TokenType;
import lti.scheme.parser.Tokenizer;
import org.junit.jupiter.api.Test;

class TokenizerTest {

  @Test
  void tokenizeAtom() {
    var tokenizer = new Tokenizer("hello");
    var token = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, token.type());
    assertEquals("hello", token.value());
    assertEquals(1, token.position().line());
    assertEquals(1, token.position().column());
    assertEquals(0, token.position().offset());

    var eof = tokenizer.nextToken();
    assertEquals(TokenType.EOF, eof.type());
    assertEquals(5, eof.position().offset());
  }

  @Test
  void tokenizeList() {
    var tokenizer = new Tokenizer("(a b)");

    var leftParen = tokenizer.nextToken();
    assertEquals(TokenType.LEFT_PAREN, leftParen.type());
    assertEquals(0, leftParen.position().offset());

    var a = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, a.type());
    assertEquals("a", a.value());
    assertEquals(1, a.position().offset());

    var b = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, b.type());
    assertEquals("b", b.value());
    assertEquals(3, b.position().offset());

    var rightParen = tokenizer.nextToken();
    assertEquals(TokenType.RIGHT_PAREN, rightParen.type());
    assertEquals(4, rightParen.position().offset());

    var eof = tokenizer.nextToken();
    assertEquals(TokenType.EOF, eof.type());
  }

  @Test
  void tokenizeBoolean() {
    var tokenizer = new Tokenizer("#t #f");

    var t = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, t.type());
    assertEquals("#t", t.value());

    var f = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, f.type());
    assertEquals("#f", f.value());
  }

  @Test
  void tokenizeDot() {
    var tokenizer = new Tokenizer("(a . b)");

    assertEquals(TokenType.LEFT_PAREN, tokenizer.nextToken().type());

    var a = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, a.type());
    assertEquals("a", a.value());

    var dot = tokenizer.nextToken();
    assertEquals(TokenType.DOT, dot.type());
    assertEquals(3, dot.position().offset());

    var b = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, b.type());
    assertEquals("b", b.value());

    assertEquals(TokenType.RIGHT_PAREN, tokenizer.nextToken().type());
  }

  @Test
  void tokenizeDotInSymbol() {
    var tokenizer = new Tokenizer("...");
    var token = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, token.type());
    assertEquals("...", token.value());
  }

  @Test
  void tokenizeQuote() {
    var tokenizer = new Tokenizer("'x");

    var quote = tokenizer.nextToken();
    assertEquals(TokenType.QUOTE, quote.type());
    assertEquals(0, quote.position().offset());

    var x = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, x.type());
    assertEquals("x", x.value());

    assertEquals(TokenType.EOF, tokenizer.nextToken().type());
  }

  @Test
  void tokenizeQuotedList() {
    var tokenizer = new Tokenizer("'(a b)");
    assertEquals(TokenType.QUOTE, tokenizer.nextToken().type());
    assertEquals(TokenType.LEFT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.RIGHT_PAREN, tokenizer.nextToken().type());
  }

  @Test
  void tokenizeEmptyInput() {
    var tokenizer = new Tokenizer("");
    var eof = tokenizer.nextToken();
    assertEquals(TokenType.EOF, eof.type());
    assertEquals(0, eof.position().offset());
  }

  @Test
  void tokenizeWhitespaceOnly() {
    var tokenizer = new Tokenizer("   \t\n  ");
    var eof = tokenizer.nextToken();
    assertEquals(TokenType.EOF, eof.type());
    assertEquals(7, eof.position().offset());
  }

  @Test
  void tokenizeNestedLists() {
    var tokenizer = new Tokenizer("((a))");
    assertEquals(TokenType.LEFT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.LEFT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.RIGHT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.RIGHT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.EOF, tokenizer.nextToken().type());
  }

  @Test
  void tokenizeDotFollowedByDigit() {
    var tokenizer = new Tokenizer(".5");
    var token = tokenizer.nextToken();
    assertEquals(TokenType.ATOM, token.type());
    assertEquals(".5", token.value());
  }

  @Test
  void tokenPositionsAreCorrect() {
    var tokenizer = new Tokenizer("(define x 42)");

    var leftParen = tokenizer.nextToken();
    assertEquals(0, leftParen.position().offset());

    var define = tokenizer.nextToken();
    assertEquals(1, define.position().offset());
    assertEquals("define", define.value());

    var x = tokenizer.nextToken();
    assertEquals(8, x.position().offset());

    var num = tokenizer.nextToken();
    assertEquals(10, num.position().offset());
    assertEquals("42", num.value());

    var rightParen = tokenizer.nextToken();
    assertEquals(12, rightParen.position().offset());
  }

  @Test
  void tokenizeFromReader() {
    var reader = new StringReader("(+ 1 2)");
    var tokenizer = new Tokenizer(reader);

    assertEquals(TokenType.LEFT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.ATOM, tokenizer.nextToken().type());
    assertEquals(TokenType.RIGHT_PAREN, tokenizer.nextToken().type());
    assertEquals(TokenType.EOF, tokenizer.nextToken().type());
  }

  @Test
  void lineAndColumnTracking() {
    var tokenizer = new Tokenizer("a\nb\nc");

    var a = tokenizer.nextToken();
    assertEquals("a", a.value());
    assertEquals(1, a.position().line());
    assertEquals(1, a.position().column());

    var b = tokenizer.nextToken();
    assertEquals("b", b.value());
    assertEquals(2, b.position().line());
    assertEquals(1, b.position().column());

    var c = tokenizer.nextToken();
    assertEquals("c", c.value());
    assertEquals(3, c.position().line());
    assertEquals(1, c.position().column());
  }

  @Test
  void columnTrackingWithinLine() {
    var tokenizer = new Tokenizer("abc def ghi");

    var abc = tokenizer.nextToken();
    assertEquals("abc", abc.value());
    assertEquals(1, abc.position().line());
    assertEquals(1, abc.position().column());

    var def = tokenizer.nextToken();
    assertEquals("def", def.value());
    assertEquals(1, def.position().line());
    assertEquals(5, def.position().column());

    var ghi = tokenizer.nextToken();
    assertEquals("ghi", ghi.value());
    assertEquals(1, ghi.position().line());
    assertEquals(9, ghi.position().column());
  }

  @Test
  void multiLineExpression() {
    var tokenizer = new Tokenizer("(define\n  x\n  42)");

    var leftParen = tokenizer.nextToken();
    assertEquals(1, leftParen.position().line());
    assertEquals(1, leftParen.position().column());

    var define = tokenizer.nextToken();
    assertEquals("define", define.value());
    assertEquals(1, define.position().line());
    assertEquals(2, define.position().column());

    var x = tokenizer.nextToken();
    assertEquals("x", x.value());
    assertEquals(2, x.position().line());
    assertEquals(3, x.position().column());

    var num = tokenizer.nextToken();
    assertEquals("42", num.value());
    assertEquals(3, num.position().line());
    assertEquals(3, num.position().column());

    var rightParen = tokenizer.nextToken();
    assertEquals(3, rightParen.position().line());
    assertEquals(5, rightParen.position().column());
  }
}
