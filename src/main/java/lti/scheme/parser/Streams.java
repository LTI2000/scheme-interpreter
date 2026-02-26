package lti.scheme.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class Streams {
  private Streams() {
  }

  public Stream<String> lines(BufferedReader reader) {
    return reader.lines().onClose(() -> {
      try {
        reader.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  record Line(int lineNumber, String content) {
  }

  public Stream<Line> numberedLines(Stream<String> lines) {
    AtomicInteger currentLineNumber = new AtomicInteger(0);
    return lines.map(content -> new Line(currentLineNumber.incrementAndGet(), content));
  }
}
