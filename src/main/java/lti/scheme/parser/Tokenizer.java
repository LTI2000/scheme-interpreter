package lti.scheme.parser;

import java.io.BufferedReader;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stream-based tokenizer for Scheme source code.
 * Transforms a stream of source lines into a stream of tokens.
 */
public final class Tokenizer {

  private Tokenizer() {} // Utility class, no instantiation

  /**
   * Tokenizes a stream of source lines into a stream of tokens.
   * Each string in the input stream represents one line of source code.
   * The stream ends with an EOF token.
   *
   * @param lines stream of source code lines
   * @return lazy stream of tokens, ending with EOF
   * @throws TokenizerException if invalid input is encountered
   */
  public static Stream<Token> tokenize(Stream<String> lines) {
    var state = new TokenizerState();
    Stream<Token> tokenStream = lines.flatMap(line -> {
      int lineNumber = state.nextLine();
      int lineStartOffset = state.getGlobalOffset();
      state.advanceOffset(line.length() + 1); // +1 for newline
      return tokenizeLine(line, lineNumber, lineStartOffset);
    });

    // Append EOF token at the end
    return Stream.concat(tokenStream, Stream.generate(() -> {
      var state2 = new TokenizerState();
      return Token.eof(new Position(1, 1, 0));
    }).limit(1)).map(token -> {
      // This is a workaround - we need to track final position for EOF
      return token;
    });
  }

  /**
   * Tokenizes a stream of source lines into a stream of tokens with proper EOF position.
   */
  public static Stream<Token> tokenizeWithEof(Stream<String> lines) {
    var state = new TokenizerState();
    var spliterator = new TokenizerSpliterator(lines.iterator(), state);
    return StreamSupport.stream(spliterator, false);
  }

  /**
   * Convenience method for tokenizing a single string.
   * Splits by newlines and tokenizes.
   *
   * @param source the source code string
   * @return lazy stream of tokens, ending with EOF
   */
  public static Stream<Token> tokenize(String source) {
    var state = new TokenizerState();
    var spliterator = new StringTokenizerSpliterator(source, state);
    return StreamSupport.stream(spliterator, false);
  }

  /**
   * Convenience method for tokenizing from a BufferedReader.
   * Reads lines lazily.
   *
   * @param reader the buffered reader
   * @return lazy stream of tokens, ending with EOF
   */
  public static Stream<Token> tokenize(BufferedReader reader) {
    return tokenizeWithEof(reader.lines());
  }

  private static Stream<Token> tokenizeLine(String line, int lineNumber, int lineStartOffset) {
    var spliterator = new LineTokenizerSpliterator(line, lineNumber, lineStartOffset);
    return StreamSupport.stream(spliterator, false);
  }

  /**
   * Mutable state for tracking position across lines.
   */
  private static class TokenizerState {
    private int lineNumber = 0;
    private int globalOffset = 0;

    int nextLine() {
      return ++lineNumber;
    }

    int getGlobalOffset() {
      return globalOffset;
    }

    void advanceOffset(int chars) {
      globalOffset += chars;
    }

    int getLineNumber() {
      return lineNumber;
    }
  }

  /**
   * Spliterator that tokenizes a single line.
   */
  private static class LineTokenizerSpliterator implements Spliterator<Token> {
    private final String line;
    private final int lineNumber;
    private final int lineStartOffset;
    private int column;
    private int offset; // offset within line

    LineTokenizerSpliterator(String line, int lineNumber, int lineStartOffset) {
      this.line = line;
      this.lineNumber = lineNumber;
      this.lineStartOffset = lineStartOffset;
      this.column = 1;
      this.offset = 0;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
      skipWhitespaceAndComments();
      if (offset >= line.length()) {
        return false;
      }

      Token token = readNextToken();
      action.accept(token);
      return true;
    }

    @Override
    public Spliterator<Token> trySplit() {
      return null; // no splitting support
    }

    @Override
    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
      return ORDERED | NONNULL;
    }

    private void skipWhitespaceAndComments() {
      while (offset < line.length()) {
        char c = line.charAt(offset);
        if (Character.isWhitespace(c)) {
          advance();
        } else if (c == ';') {
          // Skip rest of line (comment)
          offset = line.length();
          column = line.length() + 1;
          break;
        } else {
          break;
        }
      }
    }

    private Token readNextToken() {
      char c = line.charAt(offset);
      Position pos = currentPosition();

      return switch (c) {
        case '(' -> {
          advance();
          yield Token.leftParen(pos);
        }
        case ')' -> {
          advance();
          yield Token.rightParen(pos);
        }
        case '\'' -> {
          advance();
          yield Token.quote(pos);
        }
        case '"' -> readString(pos);
        case '.' -> {
          advance();
          if (offset >= line.length() || isDelimiter(line.charAt(offset))) {
            yield Token.dot(pos);
          }
          yield readAtom(pos, ".");
        }
        default -> readAtom(pos, "");
      };
    }

    private Token readString(Position start) {
      advance(); // consume opening quote
      var sb = new StringBuilder();

      while (offset < line.length()) {
        char c = line.charAt(offset);

        if (c == '"') {
          advance(); // consume closing quote
          return Token.string(sb.toString(), start);
        }

        if (c == '\\') {
          advance();
          if (offset >= line.length()) {
            throw new TokenizerException("Unterminated escape sequence", currentPosition());
          }
          char escaped = line.charAt(offset);
          sb.append(
              switch (escaped) {
                case 'n' -> '\n';
                case 't' -> '\t';
                case 'r' -> '\r';
                case '\\' -> '\\';
                case '"' -> '"';
                default ->
                    throw new TokenizerException(
                        "Unknown escape sequence: \\" + escaped, currentPosition());
              });
          advance();
        } else {
          sb.append(c);
          advance();
        }
      }

      throw new TokenizerException("Unterminated string literal", start);
    }

    private Token readAtom(Position start, String prefix) {
      var sb = new StringBuilder(prefix);
      while (offset < line.length() && !isDelimiter(line.charAt(offset))) {
        sb.append(line.charAt(offset));
        advance();
      }
      return Token.atom(sb.toString(), start);
    }

    private boolean isDelimiter(char c) {
      return Character.isWhitespace(c) || c == '(' || c == ')' || c == '\'' || c == '"' || c == ';';
    }

    private void advance() {
      offset++;
      column++;
    }

    private Position currentPosition() {
      return new Position(lineNumber, column, lineStartOffset + offset);
    }
  }

  /**
   * Spliterator for tokenizing a complete string with proper EOF handling.
   */
  private static class StringTokenizerSpliterator implements Spliterator<Token> {
    private final String source;
    private final TokenizerState state;
    private final String[] lines;
    private int currentLineIndex;
    private LineTokenizerSpliterator currentLineSpliterator;
    private boolean eofEmitted;

    StringTokenizerSpliterator(String source, TokenizerState state) {
      this.source = source;
      this.state = state;
      this.lines = source.split("\n", -1); // -1 to keep trailing empty strings
      this.currentLineIndex = 0;
      this.eofEmitted = false;
      advanceToNextLine();
    }

    private void advanceToNextLine() {
      if (currentLineIndex < lines.length) {
        int lineNumber = state.nextLine();
        int lineStartOffset = state.getGlobalOffset();
        String line = lines[currentLineIndex];
        state.advanceOffset(line.length() + (currentLineIndex < lines.length - 1 ? 1 : 0));
        currentLineSpliterator = new LineTokenizerSpliterator(line, lineNumber, lineStartOffset);
        currentLineIndex++;
      } else {
        currentLineSpliterator = null;
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
      while (currentLineSpliterator != null) {
        if (currentLineSpliterator.tryAdvance(action)) {
          return true;
        }
        advanceToNextLine();
      }

      // Emit EOF token
      if (!eofEmitted) {
        eofEmitted = true;
        int finalOffset = state.getGlobalOffset();
        int finalLine = state.getLineNumber();
        action.accept(Token.eof(new Position(Math.max(1, finalLine), 1, finalOffset)));
        return true;
      }

      return false;
    }

    @Override
    public Spliterator<Token> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
      return ORDERED | NONNULL;
    }
  }

  /**
   * Spliterator for tokenizing a stream of lines with proper EOF handling.
   */
  private static class TokenizerSpliterator implements Spliterator<Token> {
    private final java.util.Iterator<String> lineIterator;
    private final TokenizerState state;
    private LineTokenizerSpliterator currentLineSpliterator;
    private boolean eofEmitted;

    TokenizerSpliterator(java.util.Iterator<String> lineIterator, TokenizerState state) {
      this.lineIterator = lineIterator;
      this.state = state;
      this.eofEmitted = false;
      advanceToNextLine();
    }

    private void advanceToNextLine() {
      if (lineIterator.hasNext()) {
        String line = lineIterator.next();
        int lineNumber = state.nextLine();
        int lineStartOffset = state.getGlobalOffset();
        state.advanceOffset(line.length() + 1); // +1 for newline
        currentLineSpliterator = new LineTokenizerSpliterator(line, lineNumber, lineStartOffset);
      } else {
        currentLineSpliterator = null;
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
      while (currentLineSpliterator != null) {
        if (currentLineSpliterator.tryAdvance(action)) {
          return true;
        }
        advanceToNextLine();
      }

      // Emit EOF token
      if (!eofEmitted) {
        eofEmitted = true;
        int finalOffset = state.getGlobalOffset();
        int finalLine = state.getLineNumber();
        action.accept(Token.eof(new Position(Math.max(1, finalLine), 1, finalOffset)));
        return true;
      }

      return false;
    }

    @Override
    public Spliterator<Token> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
      return ORDERED | NONNULL;
    }
  }
}
