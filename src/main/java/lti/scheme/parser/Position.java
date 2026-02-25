package lti.scheme.parser;

public record Position(int line, int column, int offset) {

  public Position {
    if (line < 1) {
      throw new IllegalArgumentException("Line must be >= 1, got: " + line);
    }
    if (column < 1) {
      throw new IllegalArgumentException("Column must be >= 1, got: " + column);
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be >= 0, got: " + offset);
    }
  }

  @Override
  public String toString() {
    return line + ":" + column;
  }
}
