package lti.scheme;

@FunctionalInterface
interface Continuation<V,A> {
  A apply(V v);
}
