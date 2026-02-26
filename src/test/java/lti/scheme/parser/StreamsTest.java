package lti.scheme.parser;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamsTest {
  @Test
  void linesReturnsStreamOfLinesEndingWithNull() {
    BufferedReader reader = new BufferedReader(new StringReader("line1\nline2\nline3"));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<String> result = lines.toList();
      assertEquals(4, result.size());
      assertEquals("line1", result.get(0));
      assertEquals("line2", result.get(1));
      assertEquals("line3", result.get(2));
      assertEquals(null, result.get(3));
    }
  }

  @Test
  void linesReturnsStreamWithOnlyNullForEmptyReader() {
    BufferedReader reader = new BufferedReader(new StringReader(""));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<String> result = lines.toList();
      assertEquals(1, result.size());
      assertEquals(null, result.getFirst());
    }
  }

  @Test
  void linesClosesReaderOnStreamClose() {
    BufferedReader reader = new BufferedReader(new StringReader("test"));
    Stream<String> lines = Streams.lines(reader);
    lines.close();
    assertThrows(IOException.class, reader::read);
  }

  @Test
  void numberedLinesAddsLineNumbers() {
    Stream<String> input = Stream.of("first", "second", "third");
    List<NumberedLine> result = Streams.numberedLines(input).toList();
    assertEquals(3, result.size());
    assertEquals(new NumberedLine(1, "first"), result.get(0));
    assertEquals(new NumberedLine(2, "second"), result.get(1));
    assertEquals(new NumberedLine(3, "third"), result.get(2));
  }

  @Test
  void numberedLinesReturnsEmptyStreamForEmptyInput() {
    Stream<String> input = Stream.empty();
    List<NumberedLine> result = Streams.numberedLines(input).toList();
    assertTrue(result.isEmpty());
  }

  @Test
  void numberedLinesSingleLine() {
    Stream<String> input = Stream.of("only line");
    List<NumberedLine> result = Streams.numberedLines(input).toList();
    assertEquals(1, result.size());
    assertEquals(new NumberedLine(1, "only line"), result.getFirst());
  }

  @Test
  void numberedLineRecordHasCorrectAccessors() {
    NumberedLine line = new NumberedLine(42, "content");
    assertEquals(42, line.lineNumber());
    assertEquals("content", line.content());
  }

  @Test
  void linesAndNumberedLinesIntegration() {
    BufferedReader reader = new BufferedReader(new StringReader("alpha\nbeta\ngamma"));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<NumberedLine> result = Streams.numberedLines(lines).toList();
      assertEquals(4, result.size());
      assertEquals(new NumberedLine(1, "alpha"), result.get(0));
      assertEquals(new NumberedLine(2, "beta"), result.get(1));
      assertEquals(new NumberedLine(3, "gamma"), result.get(2));
      assertEquals(new NumberedLine(4, null), result.get(3));
    }
  }
}
