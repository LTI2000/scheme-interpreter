package lti.scheme;

import lti.scheme.Token.*;

public final class Tokenizer {
  private final String input;
  private int position;

  public Tokenizer(String input) {
    this.input = input;
    this.position = 0;
  }

  public Token nextToken() throws TokenizerException {
    skipWhitespace();

    if (position >= input.length()) {
      return new Eof();
    }

    char c = input.charAt(position);

    return switch (c) {
      case '(' -> {
        position++;
        yield new LeftParen();
      }
      case ')' -> {
        position++;
        yield new RightParen();
      }
      case '\'' -> {
        position++;
        yield new Quote();
      }
      case '.' -> {
        // Check if standalone dot (delimiter follows) or part of atom
        if (position + 1 >= input.length() || isDelimiter(input.charAt(position + 1))) {
          position++;
          yield new Dot();
        }
        yield readAtom();
      }
      default -> readAtom();
    };
  }

  private Token readAtom() {
    int start = position;
    while (position < input.length() && !isDelimiter(input.charAt(position))) {
      position++;
    }
    return new Atom(input.substring(start, position));
  }

  private boolean isDelimiter(char c) {
    return Character.isWhitespace(c) || c == '(' || c == ')' || c == '\'';
  }

  private void skipWhitespace() {
    while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
      position++;
    }
  }
}
