package lti.scheme;

import lti.scheme.Value.*;

public interface Environment {
  Value lookup(Symbol name);
}
