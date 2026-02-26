package lti.scheme.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Tokenizer {
  private Tokenizer() {
  }

  public static Stream<Token> tokens(Stream<Line> lines) {
    return lines.flatMap(line -> tokenizeLine(line).stream());
  }

  private static List<Token> tokenizeLine(Line line) {
    List<Token> tokens = new ArrayList<>();
    String content = line.content();
    int lineNum = line.lineNumber();
    int i = 0;

    while (i < content.length()) {
      char c = content.charAt(i);

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
        tokens.add(new Token.LeftParen(lineNum));
        i++;
        continue;
      }
      if (c == ')') {
        tokens.add(new Token.RightParen(lineNum));
        i++;
        continue;
      }

      // Quote
      if (c == '\'') {
        tokens.add(new Token.Quote(lineNum));
        i++;
        continue;
      }

      // String literal
      if (c == '"') {
        int start = i + 1;
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < content.length() && content.charAt(i) != '"') {
          if (content.charAt(i) == '\\' && i + 1 < content.length()) {
            i++;
            char escaped = content.charAt(i);
            switch (escaped) {
              case 'n' -> sb.append('\n');
              case 't' -> sb.append('\t');
              case 'r' -> sb.append('\r');
              case '\\' -> sb.append('\\');
              case '"' -> sb.append('"');
              default -> sb.append(escaped);
            }
          }
          else {
            sb.append(content.charAt(i));
          }
          i++;
        }
        if (i < content.length()) {
          i++; // skip closing quote
        }
        tokens.add(new Token.StringLiteral(lineNum, sb.toString()));
        continue;
      }

      // Number (including negative numbers)
      if (Character.isDigit(c) || (c == '-' && i + 1 < content.length() && Character.isDigit(content.charAt(i + 1)))) {
        int start = i;
        if (c == '-') {
          i++;
        }
        while (i < content.length() && Character.isDigit(content.charAt(i))) {
          i++;
        }
        // Check if followed by delimiter (whitespace, paren, or end)
        if (i == content.length() || isDelimiter(content.charAt(i))) {
          String numStr = content.substring(start, i);
          tokens.add(new Token.NumberLiteral(lineNum, Long.parseLong(numStr)));
          continue;
        }
        else {
          // Not a number, treat as symbol - reset and fall through
          i = start;
        }
      }

      // Boolean
      if (c == '#' && i + 1 < content.length()) {
        char next = content.charAt(i + 1);
        if (next == 't' || next == 'f') {
          // Check if followed by delimiter
          if (i + 2 == content.length() || isDelimiter(content.charAt(i + 2))) {
            tokens.add(new Token.BooleanLiteral(lineNum, next == 't'));
            i += 2;
            continue;
          }
        }
      }

      // Symbol (identifier)
      int start = i;
      while (i < content.length() && !isDelimiter(content.charAt(i))) {
        i++;
      }
      String symbol = content.substring(start, i);
      tokens.add(new Token.Symbol(lineNum, symbol));
    }

    return tokens;
  }

  private static boolean isDelimiter(char c) {
    return Character.isWhitespace(c) || c == '(' || c == ')' || c == ';' || c == '"' || c == '\'';
  }
}
