package lti.scheme;

import lti.scheme.Value.*;

public final class InitialEnvironment implements Environment {
  @Override
  public Value lookup(Symbol name) {
    throw new Error();
  }
}
