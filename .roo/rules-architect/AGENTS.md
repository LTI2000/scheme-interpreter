# Architect Mode Rules

## Extensibility Points
1. Add Value types (Number, Boolean, Pair, Procedure) as records in Value.java
2. Add Expression types (Application, Definition) as records in Expression.java
3. Implement chained environments for lexical scoping

## Type Safety
- Sealed interfaces enforce exhaustive pattern matching
- Adding new variants requires updating all switch expressions
