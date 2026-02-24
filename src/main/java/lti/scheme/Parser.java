package lti.scheme;

import lti.scheme.Token.*;
import lti.scheme.Value.*;

public final class Parser {
  private static final Symbol QUOTE = new Symbol("quote");

  private final Tokenizer tokenizer;
  private Token current;

  public Parser(Tokenizer tokenizer) throws TokenizerException {
    this.tokenizer = tokenizer;
    this.current = tokenizer.nextToken();
  }

  public static Value parse(String source) throws ParserException, TokenizerException {
    return new Parser(new Tokenizer(source)).parseValue();
  }

  public Value parseValue() throws ParserException, TokenizerException {
    return switch (current) {
      case Atom(var text) -> {
        advance();
        yield parseAtom(text);
      }
      case LeftParen() -> parseList();
      case Quote() -> parseQuote();
      case RightParen() -> throw new ParserException("Unexpected ')'");
      case Dot() -> throw new ParserException("Unexpected '.'");
      case Eof() -> throw new ParserException("Unexpected end of input");
    };
  }

  private Value parseAtom(String text) {
    return switch (text) {
      case "#t" -> new Bool(true);
      case "#f" -> new Bool(false);
      default -> new Symbol(text);
    };
  }

  private Value parseQuote() throws ParserException, TokenizerException {
    advance(); // consume quote token
    var quoted = parseValue();
    // 'expr  →  (quote expr)
    return new Pair(QUOTE, new Pair(quoted, new Nil()));
  }

  private Value parseList() throws ParserException, TokenizerException {
    advance(); // consume '('
    return parseListTail();
  }

  private Value parseListTail() throws ParserException, TokenizerException {
    return switch (current) {
      case RightParen() -> {
        advance();
        yield new Nil();
      }
      case Eof() -> throw new ParserException("Unclosed list");
      case Dot() -> {
        advance(); // consume '.'
        var cdr = parseValue();
        if (!(current instanceof RightParen)) {
          throw new ParserException("Expected ')' after dotted pair");
        }
        advance(); // consume ')'
        yield cdr;
      }
      default -> {
        var car = parseValue();
        var cdr = parseListTail();
        yield new Pair(car, cdr);
      }
    };
  }

  private void advance() throws TokenizerException {
    current = tokenizer.nextToken();
  }
}
