package lti.scheme;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;
import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorPropertyTest {

    private final Evaluator evaluator = new Evaluator();

    @Property
    void literalReturnsItsValue(@ForAll("symbols") Symbol symbol) {
        var literal = new Literal(symbol);
        var result = evaluator.eval(literal, null,v->v);
        assertEquals(symbol, result);
    }

    @Property
    void variableLookupReturnsEnvironmentValue(@ForAll("symbols") Symbol name, @ForAll("symbols") Symbol value) {
        var variable = new Variable(name);
        Environment env = n -> n.equals(name) ? value : null;
        var result = evaluator.eval(variable, env,v->v);
        assertEquals(value, result);
    }

    @Property
    void literalOfLiteralIsIdempotent(@ForAll("symbols") Symbol symbol) {
        var literal = new Literal(symbol);
        var result1 = evaluator.eval(literal, null,v->v);
        var literal2 = new Literal(result1);
        var result2 = evaluator.eval(literal2, null,v->v);
        assertEquals(result1, result2);
    }

    @Provide
    Arbitrary<Symbol> symbols() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(Symbol::new);
    }
}
