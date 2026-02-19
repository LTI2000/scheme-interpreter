package lti.scheme;

sealed interface Value {
  record Symbol(String name) implements Value {
  }
}
