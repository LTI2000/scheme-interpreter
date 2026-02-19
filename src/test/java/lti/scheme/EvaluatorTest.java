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
  void evalQuotation() {
    Value val = new Symbol("x");
    assertEquals(val, new Evaluator().eval(new Quotation(val), null, v -> v));
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
        .eval(new Conditional(new Quotation(new Bool(true)), new Quotation(yes), new Quotation(no)), null, v -> v));
    assertEquals(no, new Evaluator()
        .eval(new Conditional(new Quotation(new Bool(false)), new Quotation(yes), new Quotation(no)), null, v -> v));
  }
}
