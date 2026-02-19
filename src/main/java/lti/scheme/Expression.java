package lti.scheme;

import lti.scheme.Value.*;

sealed interface Expression {
  record Quotation(Value value) implements Expression {
  }

  record Variable(Symbol name) implements Expression {
  }

  record Conditional(Expression test, Expression consequent, Expression alternate) implements Expression {
  }

  record Abstraction(String var, Expression body) implements Expression {
  }
}
