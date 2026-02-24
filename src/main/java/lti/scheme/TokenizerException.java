package lti.scheme;

public class TokenizerException extends RuntimeException {
  private final int position;

  public TokenizerException(String message, int position) {
    super(message + " at position " + position);
    this.position = position;
  }

  public int getPosition() {
    return position;
  }
}
