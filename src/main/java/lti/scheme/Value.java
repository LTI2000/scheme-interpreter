package lti.scheme;

sealed interface Value {
  record Symbol(String name) implements Value {
  }

  record Bool(boolean bool) implements Value {
  }
}
