package lti.scheme.parser;

public class TokenizerException extends RuntimeException {
  private final Position position;

  public TokenizerException(String message, Position position) {
    super(message + " at " + position);
    this.position = position;
  }

  public TokenizerException(String message) {
    super(message);
    this.position = null;
  }

  public Position getPosition() {
    return position;
  }
}
