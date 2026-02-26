package lti.scheme.parser;

public sealed interface Token {
  int lineNumber();

  record LeftParen(int lineNumber) implements Token {
  }

  record RightParen(int lineNumber) implements Token {
  }

  record Quote(int lineNumber) implements Token {
  }

  record Symbol(int lineNumber, String name) implements Token {
  }

  record NumberLiteral(int lineNumber, long value) implements Token {
  }

  record BooleanLiteral(int lineNumber, boolean value) implements Token {
  }

  record StringLiteral(int lineNumber, String value) implements Token {
  }
}
