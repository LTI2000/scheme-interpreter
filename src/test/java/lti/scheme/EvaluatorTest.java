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
    assertEquals(val, new Evaluator().eval(new Quotation(val), null));
  }
  @Test
  void evalVariable() {
    Symbol sym = new Symbol("x");
    assertEquals(sym, new Evaluator().eval(new Variable(sym), name -> name));
  }
}
