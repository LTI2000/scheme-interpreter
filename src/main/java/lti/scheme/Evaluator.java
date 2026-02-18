package lti.scheme;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;

public final class Evaluator {
  public Value eval(Expression exp, Environment env) {
    return switch (exp) {
      case Quotation(Value value) -> value;
      case Variable(Symbol name) -> env.lookup(name);
      case Abstraction abstraction -> null;
      case Conditional conditional -> null;
    };
  }
}
