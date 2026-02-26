package lti.scheme.parser;

import lti.scheme.parser.Streams.Line;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StreamsTest {
  @Test
  void linesReturnsStreamOfLines() {
    BufferedReader reader = new BufferedReader(new StringReader("line1\nline2\nline3"));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<String> result = lines.toList();
      assertEquals(List.of("line1", "line2", "line3"), result);
    }
  }

  @Test
  void linesReturnsEmptyStreamForEmptyReader() {
    BufferedReader reader = new BufferedReader(new StringReader(""));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<String> result = lines.toList();
      assertTrue(result.isEmpty());
    }
  }

  @Test
  void linesClosesReaderOnStreamClose() throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader("test"));
    Stream<String> lines = Streams.lines(reader);
    lines.close();
    assertThrows(IOException.class, reader::read);
  }

  @Test
  void numberedLinesAddsLineNumbers() {
    Stream<String> input = Stream.of("first", "second", "third");
    List<Line> result = Streams.numberedLines(input).toList();
    
    assertEquals(3, result.size());
    assertEquals(new Line(1, "first"), result.get(0));
    assertEquals(new Line(2, "second"), result.get(1));
    assertEquals(new Line(3, "third"), result.get(2));
  }

  @Test
  void numberedLinesReturnsEmptyStreamForEmptyInput() {
    Stream<String> input = Stream.empty();
    List<Line> result = Streams.numberedLines(input).toList();
    assertTrue(result.isEmpty());
  }

  @Test
  void numberedLinesSingleLine() {
    Stream<String> input = Stream.of("only line");
    List<Line> result = Streams.numberedLines(input).toList();
    
    assertEquals(1, result.size());
    assertEquals(new Line(1, "only line"), result.getFirst());
  }

  @Test
  void lineRecordHasCorrectAccessors() {
    Line line = new Line(42, "content");
    assertEquals(42, line.lineNumber());
    assertEquals("content", line.content());
  }

  @Test
  void linesAndNumberedLinesIntegration() {
    BufferedReader reader = new BufferedReader(new StringReader("alpha\nbeta\ngamma"));
    try (Stream<String> lines = Streams.lines(reader)) {
      List<Line> result = Streams.numberedLines(lines).toList();
      
      assertEquals(3, result.size());
      assertEquals(new Line(1, "alpha"), result.get(0));
      assertEquals(new Line(2, "beta"), result.get(1));
      assertEquals(new Line(3, "gamma"), result.get(2));
    }
  }
}
