package lti.scheme.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Tokenizer {
  private Tokenizer() {}

  public static Stream<Token> tokens(Stream<NumberedLine> lines) {
    return lines.flatMap(Tokenizer::tokenizeLine);
  }

  private static Stream<Token> tokenizeLine(NumberedLine numberedLine) {
    List<Token> tokens = new ArrayList<>();
    String line = numberedLine.content();
    int lineNum = numberedLine.lineNumber();

    if (line == null) {
      return Stream.of(new Token.Eof(lineNum, 1));
    }

    int i = 0;

    while (i < line.length()) {
      char c = line.charAt(i);

      // Skip whitespace
      if (Character.isWhitespace(c)) {
        i++;
        continue;
      }

      // Comment - skip rest of line
      if (c == ';') {
        break;
      }

      // Parentheses
      if (c == '(') {
        tokens.add(new Token.LeftParen(lineNum, i + 1));
        i++;
        continue;
      }
      if (c == ')') {
        tokens.add(new Token.RightParen(lineNum, i + 1));
        i++;
        continue;
      }

      // Quote
      if (c == '\'') {
        tokens.add(new Token.Quote(lineNum, i + 1));
        i++;
        continue;
      }

      // String literal
      if (c == '"') {
        int start = i;
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < line.length() && line.charAt(i) != '"') {
          if (line.charAt(i) == '\\' && i + 1 < line.length()) {
            i++;
            char escaped = line.charAt(i);
            switch (escaped) {
              case 'n'  -> sb.append('\n');
              case 't'  -> sb.append('\t');
              case 'r'  -> sb.append('\r');
              case '\\' -> sb.append('\\');
              case '"'  -> sb.append('"');
              default   -> sb.append(escaped);
            }
          }
          else {
            sb.append(line.charAt(i));
          }
          i++;
        }
        if (i < line.length()) {
          i++; // skip closing quote
        }
        tokens.add(new Token.StringLiteral(lineNum, start + 1, sb.toString()));
        continue;
      }

      // Number (including negative numbers)
      if (Character.isDigit(c) || (c == '-' && i + 1 < line.length() && Character.isDigit(line.charAt(i + 1)))) {
        int start = i;
        if (c == '-') {
          i++;
        }
        while (i < line.length() && Character.isDigit(line.charAt(i))) {
          i++;
        }
        // Check if followed by delimiter (whitespace, paren, or end)
        if (i == line.length() || isDelimiter(line.charAt(i))) {
          String numStr = line.substring(start, i);
          tokens.add(new Token.NumberLiteral(lineNum, start + 1, Long.parseLong(numStr)));
          continue;
        }
        else {
          // Not a number, treat as symbol - reset and fall through
          i = start;
        }
      }

      // Boolean
      if (c == '#' && i + 1 < line.length()) {
        char next = line.charAt(i + 1);
        if (next == 't' || next == 'f') {
          // Check if followed by delimiter
          if (i + 2 == line.length() || isDelimiter(line.charAt(i + 2))) {
            tokens.add(new Token.BooleanLiteral(lineNum, i + 1, next == 't'));
            i += 2;
            continue;
          }
        }
      }

      // Symbol (identifier)
      int start = i;
      while (i < line.length() && !isDelimiter(line.charAt(i))) {
        i++;
      }
      String symbol = line.substring(start, i);
      tokens.add(new Token.Symbol(lineNum, start + 1, symbol));
    }

    return tokens.stream();
  }

  private static boolean isDelimiter(char c) {
    return Character.isWhitespace(c) || c == '(' || c == ')' || c == ';' || c == '"' || c == '\'';
  }
}
