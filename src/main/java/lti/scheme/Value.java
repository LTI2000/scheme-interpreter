package lti.scheme;

public sealed interface Value {
  record Symbol(String name) implements Value {
  }

  record Bool(boolean bool) implements Value {
  }

  record Str(String value) implements Value {
  }

  record Closure(Symbol formal, Expression body, Environment env) implements Value {
  }

  record Pair(Value car, Value cdr) implements Value {
  }

  record Nil() implements Value {
  }
}
