# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build Commands
```bash
mvn test -Dtest=EvaluatorTest#evalQuotation  # Run single test method
```

## Critical Project-Specific Patterns

- **Java 25 required** - Uses preview features (sealed types, record patterns in switch)
- **Wildcard imports for nested types** - Use `import lti.scheme.Value.*;` and `import lti.scheme.Expression.*;` to access records directly
- **Pattern matching switch** - Evaluator uses record deconstruction: `case Quotation(Value value) -> value;`
- **Environment is a functional interface** - Can be implemented as lambda: `name -> name`
- **Tests are package-private** - No `public` modifier on test classes
- **Incomplete implementations** - `Abstraction` and `Conditional` cases in Evaluator return null (TODO)
