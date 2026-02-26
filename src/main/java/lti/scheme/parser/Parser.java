package lti.scheme.parser;

import java.util.Iterator;
import java.util.stream.Stream;
import lti.scheme.Value;
import lti.scheme.Value.*;
import lti.scheme.parser.Token.TokenType;

public final class Parser {
  private static final Symbol QUOTE = new Symbol("quote");

  private final Iterator<Token> tokens;
  private Token current;

  public Parser(Iterator<Token> tokens) {
    this.tokens = tokens;
    advance();
  }

  public Parser(Stream<Token> tokenStream) {
    this(tokenStream.iterator());
  }

  public static Value parse(String source) throws ParserException {
    return new Parser(Tokenizer.tokenize(source)).parseValue();
  }

  public Value parseValue() throws ParserException {
    return switch (current.type()) {
      case ATOM -> {
        var text = current.value();
        advance();
        yield parseAtom(text);
      }
      case STRING -> {
        var text = current.value();
        advance();
        yield new Str(text);
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

  private Value parseQuote() throws ParserException {
    advance(); // consume quote token
    var quoted = parseValue();
    // 'expr  →  (quote expr)
    return new Pair(QUOTE, new Pair(quoted, new Nil()));
  }

  private Value parseList() throws ParserException {
    advance(); // consume '('
    return parseListTail(false);
  }

  private Value parseListTail(boolean hasElement) throws ParserException {
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

  private void advance() {
    if (tokens.hasNext()) {
      current = tokens.next();
    }
    // If no more tokens, current stays as EOF (last token should be EOF)
  }
}
