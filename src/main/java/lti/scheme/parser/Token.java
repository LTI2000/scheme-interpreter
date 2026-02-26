package lti.scheme.parser;

public sealed interface Token {
  int lineNumber();
  int columnNumber();

  record LeftParen(int lineNumber, int columnNumber) implements Token {
  }

  record RightParen(int lineNumber, int columnNumber) implements Token {
  }

  record Quote(int lineNumber, int columnNumber) implements Token {
  }

  record Symbol(int lineNumber, int columnNumber, String name) implements Token {
  }

  record NumberLiteral(int lineNumber, int columnNumber, long value) implements Token {
  }

  record BooleanLiteral(int lineNumber, int columnNumber, boolean value) implements Token {
  }

  record StringLiteral(int lineNumber, int columnNumber, String value) implements Token {
  }
}
