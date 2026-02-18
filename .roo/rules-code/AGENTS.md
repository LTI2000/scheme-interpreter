# Code Mode Rules

## Import Convention
- Use wildcard imports for nested types: `import lti.scheme.Value.*;`
- Static import for assertions: `import static org.junit.jupiter.api.Assertions.*;`

## Pattern Matching
- Use record deconstruction in switch: `case Variable(Symbol name) -> env.lookup(name);`
- Sealed interfaces ensure exhaustive matching - compiler warns on missing cases

## Adding New Types
- New Value types: Create record implementing `Value` in Value.java
- New Expression types: Create record implementing `Expression` in Expression.java
- Update Evaluator.eval() switch to handle new cases
