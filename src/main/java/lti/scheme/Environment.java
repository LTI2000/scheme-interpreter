package lti.scheme;

import lti.scheme.Value.*;

@FunctionalInterface
interface Environment {
  Value lookup(Symbol name);
}
