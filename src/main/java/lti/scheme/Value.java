package lti.scheme;

sealed interface Value {
  record Symbol(String name) implements Value {
  }

  record Bool(boolean bool) implements Value {
  }

  record Closure(Symbol formal, Expression body, Environment env) implements Value {
  }
}
