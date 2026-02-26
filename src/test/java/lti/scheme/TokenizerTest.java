package lti.scheme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;
import lti.scheme.parser.Token;
import lti.scheme.parser.Token.TokenType;
import lti.scheme.parser.Tokenizer;
import lti.scheme.parser.TokenizerException;
import org.junit.jupiter.api.Test;

class TokenizerTest {

  @Test
  void tokenizeAtom() {
    var tokens = Tokenizer.tokenize("hello").toList();
    assertEquals(2, tokens.size()); // atom + EOF

    var token = tokens.get(0);
    assertEquals(TokenType.ATOM, token.type());
    assertEquals("hello", token.value());
    assertEquals(1, token.position().line());
    assertEquals(1, token.position().column());
    assertEquals(0, token.position().offset());

    var eof = tokens.get(1);
    assertEquals(TokenType.EOF, eof.type());
    assertEquals(5, eof.position().offset());
  }

  @Test
  void tokenizeList() {
    var tokens = Tokenizer.tokenize("(a b)").toList();
    assertEquals(5, tokens.size()); // ( a b ) EOF

    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());
    assertEquals(0, tokens.get(0).position().offset());

    var a = tokens.get(1);
    assertEquals(TokenType.ATOM, a.type());
    assertEquals("a", a.value());
    assertEquals(1, a.position().offset());

    var b = tokens.get(2);
    assertEquals(TokenType.ATOM, b.type());
    assertEquals("b", b.value());
    assertEquals(3, b.position().offset());

    assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).type());
    assertEquals(4, tokens.get(3).position().offset());

    assertEquals(TokenType.EOF, tokens.get(4).type());
  }

  @Test
  void tokenizeBoolean() {
    var tokens = Tokenizer.tokenize("#t #f").toList();
    assertEquals(3, tokens.size()); // #t #f EOF

    assertEquals(TokenType.ATOM, tokens.get(0).type());
    assertEquals("#t", tokens.get(0).value());

    assertEquals(TokenType.ATOM, tokens.get(1).type());
    assertEquals("#f", tokens.get(1).value());
  }

  @Test
  void tokenizeDot() {
    var tokens = Tokenizer.tokenize("(a . b)").toList();
    assertEquals(6, tokens.size()); // ( a . b ) EOF

    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());

    var a = tokens.get(1);
    assertEquals(TokenType.ATOM, a.type());
    assertEquals("a", a.value());

    var dot = tokens.get(2);
    assertEquals(TokenType.DOT, dot.type());
    assertEquals(3, dot.position().offset());

    var b = tokens.get(3);
    assertEquals(TokenType.ATOM, b.type());
    assertEquals("b", b.value());

    assertEquals(TokenType.RIGHT_PAREN, tokens.get(4).type());
  }

  @Test
  void tokenizeDotInSymbol() {
    var tokens = Tokenizer.tokenize("...").toList();
    assertEquals(2, tokens.size()); // ... EOF

    var token = tokens.get(0);
    assertEquals(TokenType.ATOM, token.type());
    assertEquals("...", token.value());
  }

  @Test
  void tokenizeQuote() {
    var tokens = Tokenizer.tokenize("'x").toList();
    assertEquals(3, tokens.size()); // ' x EOF

    var quote = tokens.get(0);
    assertEquals(TokenType.QUOTE, quote.type());
    assertEquals(0, quote.position().offset());

    var x = tokens.get(1);
    assertEquals(TokenType.ATOM, x.type());
    assertEquals("x", x.value());

    assertEquals(TokenType.EOF, tokens.get(2).type());
  }

  @Test
  void tokenizeQuotedList() {
    var tokens = Tokenizer.tokenize("'(a b)").toList();
    assertEquals(6, tokens.size()); // ' ( a b ) EOF

    assertEquals(TokenType.QUOTE, tokens.get(0).type());
    assertEquals(TokenType.LEFT_PAREN, tokens.get(1).type());
    assertEquals(TokenType.ATOM, tokens.get(2).type());
    assertEquals(TokenType.ATOM, tokens.get(3).type());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(4).type());
  }

  @Test
  void tokenizeEmptyInput() {
    var tokens = Tokenizer.tokenize("").toList();
    assertEquals(1, tokens.size()); // just EOF

    var eof = tokens.get(0);
    assertEquals(TokenType.EOF, eof.type());
    assertEquals(0, eof.position().offset());
  }

  @Test
  void tokenizeWhitespaceOnly() {
    var tokens = Tokenizer.tokenize("   \t\n  ").toList();
    assertEquals(1, tokens.size()); // just EOF

    var eof = tokens.get(0);
    assertEquals(TokenType.EOF, eof.type());
    // Offset should be at end of input: 3 spaces + tab + newline + 2 spaces = 7 chars
    assertEquals(7, eof.position().offset());
  }

  @Test
  void tokenizeNestedLists() {
    var tokens = Tokenizer.tokenize("((a))").toList();
    assertEquals(6, tokens.size()); // ( ( a ) ) EOF

    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());
    assertEquals(TokenType.LEFT_PAREN, tokens.get(1).type());
    assertEquals(TokenType.ATOM, tokens.get(2).type());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).type());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(4).type());
    assertEquals(TokenType.EOF, tokens.get(5).type());
  }

  @Test
  void tokenizeDotFollowedByDigit() {
    var tokens = Tokenizer.tokenize(".5").toList();
    assertEquals(2, tokens.size()); // .5 EOF

    var token = tokens.get(0);
    assertEquals(TokenType.ATOM, token.type());
    assertEquals(".5", token.value());
  }

  @Test
  void tokenPositionsAreCorrect() {
    var tokens = Tokenizer.tokenize("(define x 42)").toList();

    var leftParen = tokens.get(0);
    assertEquals(0, leftParen.position().offset());

    var define = tokens.get(1);
    assertEquals(1, define.position().offset());
    assertEquals("define", define.value());

    var x = tokens.get(2);
    assertEquals(8, x.position().offset());

    var num = tokens.get(3);
    assertEquals(10, num.position().offset());
    assertEquals("42", num.value());

    var rightParen = tokens.get(4);
    assertEquals(12, rightParen.position().offset());
  }

  @Test
  void tokenizeFromBufferedReader() {
    var reader = new BufferedReader(new StringReader("(+ 1 2)"));
    var tokens = Tokenizer.tokenize(reader).toList();

    assertEquals(6, tokens.size()); // ( + 1 2 ) EOF
    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());
    assertEquals(TokenType.ATOM, tokens.get(1).type());
    assertEquals(TokenType.ATOM, tokens.get(2).type());
    assertEquals(TokenType.ATOM, tokens.get(3).type());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(4).type());
    assertEquals(TokenType.EOF, tokens.get(5).type());
  }

  @Test
  void lineAndColumnTracking() {
    var tokens = Tokenizer.tokenize("a\nb\nc").toList();

    var a = tokens.get(0);
    assertEquals("a", a.value());
    assertEquals(1, a.position().line());
    assertEquals(1, a.position().column());

    var b = tokens.get(1);
    assertEquals("b", b.value());
    assertEquals(2, b.position().line());
    assertEquals(1, b.position().column());

    var c = tokens.get(2);
    assertEquals("c", c.value());
    assertEquals(3, c.position().line());
    assertEquals(1, c.position().column());
  }

  @Test
  void columnTrackingWithinLine() {
    var tokens = Tokenizer.tokenize("abc def ghi").toList();

    var abc = tokens.get(0);
    assertEquals("abc", abc.value());
    assertEquals(1, abc.position().line());
    assertEquals(1, abc.position().column());

    var def = tokens.get(1);
    assertEquals("def", def.value());
    assertEquals(1, def.position().line());
    assertEquals(5, def.position().column());

    var ghi = tokens.get(2);
    assertEquals("ghi", ghi.value());
    assertEquals(1, ghi.position().line());
    assertEquals(9, ghi.position().column());
  }

  @Test
  void multiLineExpression() {
    var tokens = Tokenizer.tokenize("(define\n  x\n  42)").toList();

    var leftParen = tokens.get(0);
    assertEquals(1, leftParen.position().line());
    assertEquals(1, leftParen.position().column());

    var define = tokens.get(1);
    assertEquals("define", define.value());
    assertEquals(1, define.position().line());
    assertEquals(2, define.position().column());

    var x = tokens.get(2);
    assertEquals("x", x.value());
    assertEquals(2, x.position().line());
    assertEquals(3, x.position().column());

    var num = tokens.get(3);
    assertEquals("42", num.value());
    assertEquals(3, num.position().line());
    assertEquals(3, num.position().column());

    var rightParen = tokens.get(4);
    assertEquals(3, rightParen.position().line());
    assertEquals(5, rightParen.position().column());
  }

  // New tests for string literals

  @Test
  void tokenizeString() {
    var tokens = Tokenizer.tokenize("\"hello\"").toList();
    assertEquals(2, tokens.size()); // string + EOF

    var token = tokens.get(0);
    assertEquals(TokenType.STRING, token.type());
    assertEquals("hello", token.value());
  }

  @Test
  void tokenizeStringWithEscapes() {
    var tokens = Tokenizer.tokenize("\"hello\\nworld\"").toList();
    assertEquals(2, tokens.size());

    var token = tokens.get(0);
    assertEquals(TokenType.STRING, token.type());
    assertEquals("hello\nworld", token.value());
  }

  @Test
  void tokenizeStringWithAllEscapes() {
    var tokens = Tokenizer.tokenize("\"\\t\\r\\n\\\\\\\"\"").toList();
    assertEquals(2, tokens.size());

    var token = tokens.get(0);
    assertEquals(TokenType.STRING, token.type());
    assertEquals("\t\r\n\\\"", token.value());
  }

  @Test
  void tokenizeEmptyString() {
    var tokens = Tokenizer.tokenize("\"\"").toList();
    assertEquals(2, tokens.size());

    var token = tokens.get(0);
    assertEquals(TokenType.STRING, token.type());
    assertEquals("", token.value());
  }

  @Test
  void tokenizeStringInList() {
    var tokens = Tokenizer.tokenize("(print \"hello\")").toList();
    assertEquals(5, tokens.size()); // ( print "hello" ) EOF

    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());
    assertEquals(TokenType.ATOM, tokens.get(1).type());
    assertEquals("print", tokens.get(1).value());
    assertEquals(TokenType.STRING, tokens.get(2).type());
    assertEquals("hello", tokens.get(2).value());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).type());
  }

  @Test
  void unterminatedStringThrows() {
    assertThrows(TokenizerException.class, () -> Tokenizer.tokenize("\"hello").toList());
  }

  @Test
  void unknownEscapeSequenceThrows() {
    assertThrows(TokenizerException.class, () -> Tokenizer.tokenize("\"\\x\"").toList());
  }

  // New tests for comments

  @Test
  void skipLineComment() {
    var tokens = Tokenizer.tokenize("a ; comment\nb").toList();
    assertEquals(3, tokens.size()); // a b EOF

    assertEquals("a", tokens.get(0).value());
    assertEquals("b", tokens.get(1).value());
    assertEquals(TokenType.EOF, tokens.get(2).type());
  }

  @Test
  void skipFullLineComment() {
    var tokens = Tokenizer.tokenize("; comment\na").toList();
    assertEquals(2, tokens.size()); // a EOF

    assertEquals("a", tokens.get(0).value());
  }

  @Test
  void skipCommentAtEndOfInput() {
    var tokens = Tokenizer.tokenize("a ; comment").toList();
    assertEquals(2, tokens.size()); // a EOF

    assertEquals("a", tokens.get(0).value());
    assertEquals(TokenType.EOF, tokens.get(1).type());
  }

  @Test
  void commentOnlyInput() {
    var tokens = Tokenizer.tokenize("; just a comment").toList();
    assertEquals(1, tokens.size()); // just EOF

    assertEquals(TokenType.EOF, tokens.get(0).type());
  }

  // Test for stream-based API

  @Test
  void tokenizeFromStreamOfLines() {
    Stream<String> lines = Stream.of("(define x", "  42)");
    var tokens = Tokenizer.tokenizeWithEof(lines).toList();

    assertEquals(6, tokens.size()); // ( define x 42 ) EOF
    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).type());
    assertEquals("define", tokens.get(1).value());
    assertEquals("x", tokens.get(2).value());
    assertEquals("42", tokens.get(3).value());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(4).type());
    assertEquals(TokenType.EOF, tokens.get(5).type());
  }

  @Test
  void globalOffsetAcrossLines() {
    var tokens = Tokenizer.tokenize("a\nb").toList();

    assertEquals(0, tokens.get(0).position().offset()); // 'a' at offset 0
    assertEquals(2, tokens.get(1).position().offset()); // 'b' at offset 2 (after 'a' and '\n')
  }

  @Test
  void stringIsDelimiter() {
    var tokens = Tokenizer.tokenize("a\"b\"c").toList();
    assertEquals(4, tokens.size()); // a "b" c EOF

    assertEquals(TokenType.ATOM, tokens.get(0).type());
    assertEquals("a", tokens.get(0).value());
    assertEquals(TokenType.STRING, tokens.get(1).type());
    assertEquals("b", tokens.get(1).value());
    assertEquals(TokenType.ATOM, tokens.get(2).type());
    assertEquals("c", tokens.get(2).value());
  }

  @Test
  void semicolonIsDelimiter() {
    var tokens = Tokenizer.tokenize("a;comment").toList();
    assertEquals(2, tokens.size()); // a EOF

    assertEquals(TokenType.ATOM, tokens.get(0).type());
    assertEquals("a", tokens.get(0).value());
  }
}
