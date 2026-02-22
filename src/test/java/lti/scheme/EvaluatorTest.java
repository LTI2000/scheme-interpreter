package lti.scheme;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;

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
  void evalConditional() {
    Symbol yes = new Symbol("yes");
    Symbol no = new Symbol("no");
    assertEquals(yes, new Evaluator()
        .eval(new Conditional(new Literal(new Bool(true)), new Literal(yes), new Literal(no)), null, v -> v));
    assertEquals(no, new Evaluator()
        .eval(new Conditional(new Literal(new Bool(false)), new Literal(yes), new Literal(no)), null, v -> v));
  }

  @Test
  void evalAbstraction() {
    Value closure = new Evaluator()
        .eval(new Abstraction(new Symbol("x"), new Variable(new Symbol("x"))), new Environment(){

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
}
