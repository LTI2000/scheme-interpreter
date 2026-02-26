package lti.scheme.parser;

public record Token(TokenType type, String value, Position position) {

  public enum TokenType {
    LEFT_PAREN,
    RIGHT_PAREN,
    DOT,
    QUOTE,
    ATOM,
    STRING,
    EOF
  }

  public static Token leftParen(Position position) {
    return new Token(TokenType.LEFT_PAREN, "(", position);
  }

  public static Token rightParen(Position position) {
    return new Token(TokenType.RIGHT_PAREN, ")", position);
  }

  public static Token dot(Position position) {
    return new Token(TokenType.DOT, ".", position);
  }

  public static Token quote(Position position) {
    return new Token(TokenType.QUOTE, "'", position);
  }

  public static Token atom(String value, Position position) {
    return new Token(TokenType.ATOM, value, position);
  }

  public static Token string(String value, Position position) {
    return new Token(TokenType.STRING, value, position);
  }

  public static Token eof(Position position) {
    return new Token(TokenType.EOF, "", position);
  }
}
