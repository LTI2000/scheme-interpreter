package lti.scheme.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class Tokenizer {
  private final Reader reader;
  private int currentChar;
  private int offset;
  private int line;
  private int column;

  public Tokenizer(Reader reader) throws TokenizerException {
    this.reader = reader;
    this.offset = 0;
    this.line = 1;
    this.column = 1;
    readNextChar();
  }

  public Tokenizer(String input) throws TokenizerException {
    this(new StringReader(input));
  }

  public Token nextToken() throws TokenizerException {
    skipWhitespace();

    if (currentChar == -1) {
      return Token.eof(currentPosition());
    }

    char c = (char) currentChar;
    Position tokenStart = currentPosition();

    return switch (c) {
      case '(' -> {
        advance();
        yield Token.leftParen(tokenStart);
      }
      case ')' -> {
        advance();
        yield Token.rightParen(tokenStart);
      }
      case '\'' -> {
        advance();
        yield Token.quote(tokenStart);
      }
      case '.' -> {
        advance();
        // Check if standalone dot (delimiter follows) or part of atom
        if (currentChar == -1 || isDelimiter((char) currentChar)) {
          yield Token.dot(tokenStart);
        }
        yield readAtom(tokenStart, ".");
      }
      default -> readAtom(tokenStart, "");
    };
  }

  private Token readAtom(Position start, String prefix) throws TokenizerException {
    var sb = new StringBuilder(prefix);
    while (currentChar != -1 && !isDelimiter((char) currentChar)) {
      sb.append((char) currentChar);
      advance();
    }
    return Token.atom(sb.toString(), start);
  }

  private boolean isDelimiter(char c) {
    return Character.isWhitespace(c) || c == '(' || c == ')' || c == '\'';
  }

  private void skipWhitespace() throws TokenizerException {
    while (currentChar != -1 && Character.isWhitespace((char) currentChar)) {
      advance();
    }
  }

  private void readNextChar() throws TokenizerException {
    try {
      currentChar = reader.read();
    } catch (IOException e) {
      throw new TokenizerException("Error reading input: " + e.getMessage());
    }
  }

  private void advance() throws TokenizerException {
    if (currentChar == -1) {
      return;
    }

    boolean wasNewline = currentChar == '\n';
    offset++;

    if (wasNewline) {
      line++;
      column = 1;
    } else {
      column++;
    }

    readNextChar();
  }

  private Position currentPosition() {
    return new Position(line, column, offset);
  }
}
