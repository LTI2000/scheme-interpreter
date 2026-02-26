package lti.scheme;

import lti.scheme.Expression.Abstraction;
import lti.scheme.Expression.Application;
import lti.scheme.Expression.Conditional;
import lti.scheme.Expression.Literal;
import lti.scheme.Expression.Variable;
import lti.scheme.Value.Bool;
import lti.scheme.Value.Symbol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorTest {
  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void evalLiteral() {
    Value val = new Symbol("x");
    assertEquals(val, new Evaluator().eval(new Literal(val), null, v -> v));
  }

  @Test
  void evalVariable() {
    Symbol sym = new Symbol("x");
    assertEquals(sym, new Evaluator().eval(new Variable(sym), name -> name, v -> v));
  }

  @Test
  void evalAbstraction() {
    Value closure = new Evaluator().eval(new Abstraction(new Symbol("x"), new Variable(new Symbol("x"))), new Environment() {
      @Override
      public Value lookup(Symbol name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lookup'");
      }

      public String toString() {
        return "#<environment>";
      }
    }, v -> v);
    assertEquals("Closure[formal=Symbol[name=x], body=Variable[name=Symbol[name=x]], env=#<environment>]", closure.toString());
  }

  @Test
  void evalApplication() {
    assertEquals(new Bool(false), new Evaluator().eval(new Application(new Abstraction(new Symbol("x"), new Variable(new Symbol("x"))), new Literal(new Bool(false))), new InitialEnvironment(), v -> v));
  }

  @Test
  void evalConditional() {
    Symbol yes = new Symbol("yes");
    Symbol no = new Symbol("no");
    assertEquals(yes, new Evaluator().eval(new Conditional(new Literal(new Bool(true)), new Literal(yes), new Literal(no)), null, v -> v));
    assertEquals(no, new Evaluator().eval(new Conditional(new Literal(new Bool(false)), new Literal(yes), new Literal(no)), null, v -> v));
  }
}
