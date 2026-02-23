package lti.scheme;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;

public final class Evaluator {
  public Value eval(Expression exp, Environment env, Continuation<Value, Value> k) {
    return switch (exp) {
      case Literal(var value) -> k.apply(value);
      case Variable(var name) -> k.apply(env.lookup(name));
      case Abstraction(var formal, var body) -> k.apply(new Closure(formal, body, env));
      case Application(var operator, var operand) ->
          eval(operator, env, proc -> eval(operand, env, arg -> apply(proc, arg, k)));
      case Conditional(var test, var consequent, var alternate) ->
          eval(test, env, t -> isTrue(t) ? eval(consequent, env, k) : eval(alternate, env, k));
    };
  }

  public Value apply(Value proc, Value arg, Continuation<Value, Value> k) {
    return null;
  }

  private boolean isTrue(Value value) {
    return switch (value) {
      case Bool(var bool) when !bool -> false;
      default -> true;
    };
  }
}
