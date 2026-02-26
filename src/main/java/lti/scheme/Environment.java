package lti.scheme;

import lti.scheme.Value.Symbol;

@FunctionalInterface
interface Environment {
  Value lookup(Symbol name);

  default Environment extend(Symbol name, Value value) {
    return n -> name.equals(n) ? value : lookup(name);
  }
}
