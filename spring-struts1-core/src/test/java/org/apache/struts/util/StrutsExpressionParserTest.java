package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.struts.util.StrutsExpressionParser.Segment;

/**
 * Unit tests of the beanutils-style parameter-name parser: simple,
 * indexed, mapped, nested names and their combinations, including keys and
 * indexes containing dots and the unparseable-name fallback.
 */
class StrutsExpressionParserTest {

    private static Segment single(String name) {
        List<Segment> segments = StrutsExpressionParser.parse(name);
        assertEquals(1, segments.size(), "a single segment is expected");
        return segments.get(0);
    }

    @Test
    void simpleNameHasNoIndexOrKey() {
        Segment segment = single("name");
        assertEquals("name", segment.getName());
        assertNull(segment.getIndex());
        assertNull(segment.getKey());
    }

    @Test
    void indexedNameParsesIndex() {
        Segment segment = single("intIndexed[0]");
        assertEquals("intIndexed", segment.getName());
        assertEquals(0, segment.getIndex());
        assertNull(segment.getKey());
    }

    @Test
    void mappedNameParsesKey() {
        Segment segment = single("mapValue(color)");
        assertEquals("mapValue", segment.getName());
        assertNull(segment.getIndex());
        assertEquals("color", segment.getKey());
    }

    @Test
    void mappedKeyMayContainDotsAndBrackets() {
        Segment segment = single("map(a.b[1])");
        assertEquals("map", segment.getName());
        assertEquals("a.b[1]", segment.getKey());
    }

    @Test
    void indexedAndMappedCombineInOneSegment() {
        Segment segment = single("maps[0](color)");
        assertEquals("maps", segment.getName());
        assertEquals(0, segment.getIndex());
        assertEquals("color", segment.getKey());
    }

    @Test
    void nestedPathSplitsIntoSegments() {
        List<Segment> segments = StrutsExpressionParser.parse(
            "nested.intProperty");
        assertEquals(2, segments.size());
        assertEquals("nested", segments.get(0).getName());
        assertEquals("intProperty", segments.get(1).getName());
    }

    @Test
    void fullCombinationParsesAllSegments() {
        List<Segment> segments = StrutsExpressionParser.parse(
            "a.b[0](key)");
        assertEquals(2, segments.size());
        assertEquals("a", segments.get(0).getName());
        Segment last = segments.get(1);
        assertEquals("b", last.getName());
        assertEquals(0, last.getIndex());
        assertEquals("key", last.getKey());
    }

    @Test
    void negativeIndexParses() {
        Segment segment = single("list[-1]");
        assertEquals(-1, segment.getIndex());
    }

    @Test
    void unbalancedBracketYieldsNoSegments() {
        assertTrue(StrutsExpressionParser.parse("mapValue(color")
            .isEmpty());
    }

    @Test
    void emptySegmentYieldsNoSegments() {
        assertTrue(StrutsExpressionParser.parse("a..b").isEmpty());
        assertTrue(StrutsExpressionParser.parse("[0]").isEmpty());
    }
}
