# Debug Mode Rules

## Known Incomplete Implementations
- `Evaluator.eval()` returns null for `Abstraction` and `Conditional` cases
- These are intentional TODOs, not bugs

## Test Execution
- Single test: `mvn test -Dtest=EvaluatorTest#methodName`
- Environment can be null for quotation tests (doesn't use lookup)
