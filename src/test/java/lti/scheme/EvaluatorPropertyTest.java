package lti.scheme;

import lti.scheme.Expression.*;
import lti.scheme.Value.*;
import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorPropertyTest {

    private final Evaluator evaluator = new Evaluator();

    @Property
    void quotationReturnsItsValue(@ForAll("symbols") Symbol symbol) {
        var quotation = new Quotation(symbol);
        var result = evaluator.eval(quotation, null);
        assertEquals(symbol, result);
    }

    @Property
    void variableLookupReturnsEnvironmentValue(@ForAll("symbols") Symbol name, @ForAll("symbols") Symbol value) {
        var variable = new Variable(name);
        Environment env = n -> n.equals(name) ? value : null;
        var result = evaluator.eval(variable, env);
        assertEquals(value, result);
    }

    @Property
    void quotationOfQuotationIsIdempotent(@ForAll("symbols") Symbol symbol) {
        var quotation = new Quotation(symbol);
        var result1 = evaluator.eval(quotation, null);
        var quotation2 = new Quotation(result1);
        var result2 = evaluator.eval(quotation2, null);
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
