package lti.scheme.parser;

import lti.scheme.Value;
import lti.scheme.Value.*;
import lti.scheme.parser.Token.TokenType;

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
    return switch (current.type()) {
      case ATOM -> {
        var text = current.value();
        advance();
        yield parseAtom(text);
      }
      case LEFT_PAREN -> parseList();
      case QUOTE -> parseQuote();
      case RIGHT_PAREN -> throw new ParserException("Unexpected ')'");
      case DOT -> throw new ParserException("Unexpected '.'");
      case EOF -> throw new ParserException("Unexpected end of input");
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
    return parseListTail(false);
  }

  private Value parseListTail(boolean hasElement) throws ParserException, TokenizerException {
    return switch (current.type()) {
      case RIGHT_PAREN -> {
        advance();
        yield new Nil();
      }
      case EOF -> throw new ParserException("Unclosed list");
      case DOT -> {
        if (!hasElement) {
          throw new ParserException("Unexpected '.' at start of list");
        }
        advance(); // consume '.'
        var cdr = parseValue();
        if (current.type() != TokenType.RIGHT_PAREN) {
          throw new ParserException("Expected ')' after dotted pair");
        }
        advance(); // consume ')'
        yield cdr;
      }
      default -> {
        var car = parseValue();
        var cdr = parseListTail(true);
        yield new Pair(car, cdr);
      }
    };
  }

  private void advance() throws TokenizerException {
    current = tokenizer.nextToken();
  }
}
