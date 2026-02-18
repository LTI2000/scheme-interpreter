# Scheme Interpreter

A Java-based Scheme interpreter leveraging modern Java features including sealed types, record patterns, and pattern matching in switch expressions.

## Features

- **Sealed Type Hierarchy**: Uses Java's sealed interfaces for type-safe expression and value representations
- **Pattern Matching**: Evaluator uses record deconstruction in switch expressions for clean, exhaustive pattern matching
- **Functional Environment**: Environment is a functional interface, allowing lambda-based implementations
- **Expression Types**:
  - `Quotation` - Quoted values that evaluate to themselves
  - `Variable` - Symbol lookup in the environment
  - `Conditional` - If-then-else expressions (in progress)
  - `Abstraction` - Lambda expressions (in progress)
- **Value Types**:
  - `Symbol` - Symbolic names

## Requirements

- **Java 25** with preview features enabled
- **Maven 3.6+**

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd scheme-interpreter
   ```

2. Build the project:
   ```bash
   mvn compile
   ```

3. Run tests:
   ```bash
   mvn test
   ```

## Usage

### Running Tests

Run all tests:
```bash
mvn test
```

Run a specific test method:
```bash
mvn test -Dtest=EvaluatorTest#evalQuotation
```

### Code Examples

#### Evaluating a Quotation
```java
import lti.scheme.*;
import lti.scheme.Expression.*;
import lti.scheme.Value.*;

Value val = new Symbol("x");
Value result = new Evaluator().eval(new Quotation(val), null);
// result equals Symbol("x")
```

#### Variable Lookup with Lambda Environment
```java
import lti.scheme.*;
import lti.scheme.Expression.*;
import lti.scheme.Value.*;

Symbol sym = new Symbol("x");
// Environment as lambda - returns the symbol name as its value
Value result = new Evaluator().eval(new Variable(sym), name -> name);
// result equals Symbol("x")
```

## Project Structure

```
scheme-interpreter/
├── pom.xml                              # Maven build configuration
├── LICENSE                              # Unlicense (public domain)
├── AGENTS.md                            # Agent guidance documentation
├── src/
│   ├── main/java/lti/
│   │   ├── Main.java                    # Application entry point
│   │   └── scheme/
│   │       ├── Environment.java         # Functional interface for variable lookup
│   │       ├── Evaluator.java           # Expression evaluator with pattern matching
│   │       ├── Expression.java          # Sealed interface with expression types
│   │       └── Value.java               # Sealed interface with value types
│   └── test/java/lti/scheme/
│       └── EvaluatorTest.java           # JUnit 5 tests for the evaluator
```

### Key Components

| Component | Description |
|-----------|-------------|
| [`Environment`](src/main/java/lti/scheme/Environment.java) | Functional interface for symbol lookup |
| [`Evaluator`](src/main/java/lti/scheme/Evaluator.java) | Core evaluation logic using pattern matching switch |
| [`Expression`](src/main/java/lti/scheme/Expression.java) | Sealed interface defining all expression types |
| [`Value`](src/main/java/lti/scheme/Value.java) | Sealed interface defining all value types |

## Development

### Import Conventions

Use wildcard imports for nested types:
```java
import lti.scheme.Value.*;
import lti.scheme.Expression.*;
```

Static import for assertions in tests:
```java
import static org.junit.jupiter.api.Assertions.*;
```

### Adding New Types

1. **New Value types**: Create a record implementing `Value` in [`Value.java`](src/main/java/lti/scheme/Value.java)
2. **New Expression types**: Create a record implementing `Expression` in [`Expression.java`](src/main/java/lti/scheme/Expression.java)
3. Update [`Evaluator.eval()`](src/main/java/lti/scheme/Evaluator.java) switch to handle new cases

The sealed interfaces ensure exhaustive matching - the compiler will warn on missing cases.

## License

This project is released into the public domain under the [Unlicense](LICENSE). See the LICENSE file for details.
