package lti.scheme;

sealed interface Token {
  record LeftParen() implements Token {}

  record RightParen() implements Token {}

  record Dot() implements Token {}

  record Quote() implements Token {}

  record Atom(String text) implements Token {}

  record Eof() implements Token {}
}
