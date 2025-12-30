package com.zuomagai.molamola.test.string;

import com.zuomagai.molamola.string.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StringUtilsTest {

    @Test
    public void testEmptyAndBlank() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty(" "));

        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" \t\n"));
        Assert.assertFalse(StringUtils.isBlank(" a "));
    }

    @Test
    public void testTrimAndDefault() {
        Assert.assertEquals("", StringUtils.trimToEmpty(null));
        Assert.assertEquals("a", StringUtils.trimToEmpty(" a "));
        Assert.assertNull(StringUtils.trimToNull("  "));
        Assert.assertEquals("a", StringUtils.trimToNull(" a "));

        Assert.assertEquals("x", StringUtils.defaultIfNull(null, "x"));
        Assert.assertEquals("y", StringUtils.defaultIfNull("y", "x"));
        Assert.assertEquals("x", StringUtils.defaultIfBlank("  ", "x"));
        Assert.assertEquals("y", StringUtils.defaultIfBlank("y", "x"));
    }

    @Test
    public void testEqualsAndContains() {
        Assert.assertTrue(StringUtils.equals(null, null));
        Assert.assertFalse(StringUtils.equals(null, "a"));
        Assert.assertTrue(StringUtils.equals("a", "a"));
        Assert.assertTrue(StringUtils.equalsIgnoreCase("Ab", "aB"));

        Assert.assertTrue(StringUtils.contains("abc", "b"));
        Assert.assertFalse(StringUtils.contains(null, "b"));
        Assert.assertFalse(StringUtils.contains("abc", null));
        Assert.assertTrue(StringUtils.containsIgnoreCase("AbC", "bc"));
        Assert.assertFalse(StringUtils.containsIgnoreCase(null, "a"));
    }

    @Test
    public void testJoinAndRepeat() {
        Assert.assertEquals("a,,c", StringUtils.join(new Object[]{"a", null, "c"}, ","));
        Assert.assertEquals("", StringUtils.join((Object[]) null, ","));

        List<String> values = Arrays.asList("a", "b", "c");
        Assert.assertEquals("a|b|c", StringUtils.join(values, "|"));
        Assert.assertEquals("", StringUtils.join((Iterable<?>) null, ","));

        Assert.assertEquals("abab", StringUtils.repeat("ab", 2));
        Assert.assertEquals("", StringUtils.repeat("ab", 0));
        Assert.assertNull(StringUtils.repeat(null, 2));
    }
}
