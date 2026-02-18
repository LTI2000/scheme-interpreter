package lti.scheme;

public sealed interface Value {
  record Symbol(String name) implements Value {
  }
}
