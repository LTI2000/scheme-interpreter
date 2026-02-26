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
- **Wildcard imports for nested types** - Use `import lti.scheme.Value.*;` and `import lti.scheme.Expression.*;`
- **Pattern matching switch** - Evaluator uses record deconstruction: `case Literal(var value) -> k.apply(value);`
- **Continuation-passing style** - Evaluator uses `Continuation<Value, Value>` for control flow
- **Environment is a functional interface** - Can be implemented as lambda: `name -> value`
- **Tests are package-private** - No `public` modifier on test classes
- **Static imports for assertions** - Use `import static org.junit.jupiter.api.Assertions.*;`

## Testing Frameworks

- **JUnit Jupiter 6.1.0-M1** - Standard unit testing
- **jqwik 1.8.2** - Property-based testing with `@Property`, `@ForAll`, `@Provide` annotations

## Type Hierarchy

### Expression Types (sealed interface)
| Type | Description |
|------|-------------|
| `Literal(Value)` | Returns the literal value |
| `Variable(Symbol)` | Looks up symbol in environment |
| `Abstraction(Symbol formal, Expression body)` | Lambda expression |
| `Application(Expression operator, Expression operand)` | Function application |
| `Conditional(Expression test, consequent, alternate)` | If-then-else expression |

### Value Types (sealed interface)
| Type | Description |
|------|-------------|
| `Symbol(String name)` | Symbolic name |
| `Bool(boolean bool)` | Boolean value |
| `Str(String value)` | String value |
| `Closure(Symbol formal, Expression body, Environment env)` | Lambda closure |
| `Pair(Value car, Value cdr)` | Cons pair |
| `Nil()` | Empty list |

### Token Types (sealed interface, in parser package)
| Type | Description |
|------|-------------|
| `LeftParen`, `RightParen` | Parentheses |
| `Quote` | Quote character `'` |
| `Symbol(String name)` | Identifier |
| `NumberLiteral(long value)` | Integer literal |
| `BooleanLiteral(boolean value)` | `#t` or `#f` |
| `StringLiteral(String value)` | String literal |
| `Eof` | End of input |
