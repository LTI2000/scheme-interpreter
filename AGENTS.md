# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build Commands
```bash
mvn compile                                  # Compile the project
mvn test                                     # Run all tests (JUnit + jqwik)
mvn test -Dtest=EvaluatorTest#evalQuotation  # Run single test method
```

## Critical Project-Specific Patterns

- **Java 25 required** - Uses preview features (sealed types, record patterns in switch)
- **Wildcard imports for nested types** - Use `import lti.scheme.Value.*;` and `import lti.scheme.Expression.*;` to access records directly
- **Pattern matching switch** - Evaluator uses record deconstruction: `case Quotation(Value value) -> value;`
- **Environment is a functional interface** - Can be implemented as lambda: `name -> name`
- **Tests are package-private** - No `public` modifier on test classes
- **Static imports for assertions** - Use `import static org.junit.jupiter.api.Assertions.*;`
- **Incomplete implementations** - `Abstraction` and `Conditional` cases in Evaluator return null (TODO)

## Testing Frameworks

- **JUnit Jupiter 6.1.0-M1** - Standard unit testing
- **jqwik 1.8.2** - Property-based testing with `@Property`, `@ForAll`, `@Provide` annotations

## Type Hierarchy

### Expression Types (sealed interface)
| Type | Status | Description |
|------|--------|-------------|
| `Quotation(Value)` | ✅ Implemented | Returns the quoted value |
| `Variable(Symbol)` | ✅ Implemented | Looks up symbol in environment |
| `Conditional(test, consequent, alternate)` | ❌ TODO | If-then-else expression |
| `Abstraction(var, body)` | ❌ TODO | Lambda expression |

### Value Types (sealed interface)
| Type | Description |
|------|-------------|
| `Symbol(String name)` | Symbolic name |
