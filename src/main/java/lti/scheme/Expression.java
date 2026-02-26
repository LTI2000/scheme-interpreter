package lti.scheme;

import lti.scheme.Value.Symbol;

sealed interface Expression {
  record Literal(Value value) implements Expression {}

  record Variable(Symbol name) implements Expression {}

  record Abstraction(Symbol formal, Expression body) implements Expression {}

  record Application(Expression operator, Expression operand) implements Expression {}

  record Conditional(Expression test, Expression consequent, Expression alternate) implements Expression {}
}
