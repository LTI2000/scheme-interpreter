package lti.scheme;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;

public final class Evaluator {
  public Value eval(Expression exp, Environment env, Continuation<Value,Value> k) {
    return switch (exp) {
      case Quotation(var value) -> value;
      case Variable(var name) -> env.lookup(name);
      case Abstraction abstraction -> null;
      case Conditional conditional -> null;
    };
  }
}
