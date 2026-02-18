# Ask Mode Rules

## Architecture
- Sealed interface hierarchy: Expression (AST) and Value (runtime)
- Records provide immutability and automatic equals/hashCode/toString
- Environment is functional interface for variable lookup

## Key Files
- `Evaluator.java` - Core evaluation logic with pattern matching
- `Expression.java` - AST node types (Quotation, Variable, Conditional, Abstraction)
- `Value.java` - Runtime value types (currently only Symbol)
