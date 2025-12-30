package com.zuomagai.molamola.test.collection;

import com.zuomagai.molamola.collection.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtilsTest {

    @Test
    public void testEmptyAndSize() {
        Assert.assertTrue(CollectionUtils.isEmpty((List<?>) null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.isEmpty(Arrays.asList("a")));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Arrays.asList("a")));

        Assert.assertTrue(CollectionUtils.isEmpty((Map<?, ?>) null));
        Assert.assertTrue(CollectionUtils.isEmpty(Collections.emptyMap()));
        Assert.assertFalse(CollectionUtils.isEmpty(Collections.singletonMap("k", "v")));
        Assert.assertTrue(CollectionUtils.isNotEmpty(Collections.singletonMap("k", "v")));

        Assert.assertEquals(0, CollectionUtils.size((List<?>) null));
        Assert.assertEquals(2, CollectionUtils.size(Arrays.asList("a", "b")));
        Assert.assertEquals(0, CollectionUtils.size((Map<?, ?>) null));
        Assert.assertEquals(1, CollectionUtils.size(Collections.singletonMap("k", "v")));
    }

    @Test
    public void testContains() {
        List<String> base = Arrays.asList("a", "b", "c");
        Assert.assertTrue(CollectionUtils.contains(base, "b"));
        Assert.assertFalse(CollectionUtils.contains(base, "x"));
        Assert.assertFalse(CollectionUtils.contains(null, "a"));

        Assert.assertTrue(CollectionUtils.containsAny(base, Arrays.asList("x", "b")));
        Assert.assertFalse(CollectionUtils.containsAny(base, Arrays.asList("x", "y")));
        Assert.assertFalse(CollectionUtils.containsAny(null, Arrays.asList("a")));
        Assert.assertFalse(CollectionUtils.containsAny(base, Collections.emptyList()));

        Assert.assertTrue(CollectionUtils.containsAll(base, Arrays.asList("a", "b")));
        Assert.assertFalse(CollectionUtils.containsAll(base, Arrays.asList("a", "x")));
        Assert.assertTrue(CollectionUtils.containsAll(base, Collections.emptyList()));
        Assert.assertFalse(CollectionUtils.containsAll(null, Arrays.asList("a")));
    }

    @Test
    public void testFirstAndLast() {
        List<String> base = Arrays.asList("a", "b", "c");
        Assert.assertEquals("a", CollectionUtils.firstOrNull(base));
        Assert.assertEquals("c", CollectionUtils.lastOrNull(base));
        Assert.assertNull(CollectionUtils.firstOrNull(null));
        Assert.assertNull(CollectionUtils.lastOrNull(Collections.<String>emptyList()));
    }

    @Test
    public void testEmptyIfNull() {
        Assert.assertTrue(CollectionUtils.emptyIfNull((List<String>) null).isEmpty());
        Assert.assertTrue(CollectionUtils.emptyIfNull((Set<String>) null).isEmpty());
        Assert.assertTrue(CollectionUtils.emptyIfNull((Map<String, String>) null).isEmpty());

        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        Map<String, String> map = new HashMap<>();
        Assert.assertSame(list, CollectionUtils.emptyIfNull(list));
        Assert.assertSame(set, CollectionUtils.emptyIfNull(set));
        Assert.assertSame(map, CollectionUtils.emptyIfNull(map));
    }

    @Test
    public void testAddAll() {
        List<String> target = new ArrayList<>();
        target.add("a");
        int added = CollectionUtils.addAll(target, Arrays.asList("b", "c"));
        Assert.assertEquals(2, added);
        Assert.assertEquals(Arrays.asList("a", "b", "c"), target);

        Iterable<String> iterable = () -> Arrays.asList("d", "e").iterator();
        Assert.assertEquals(2, CollectionUtils.addAll(target, iterable));

        Assert.assertEquals(0, CollectionUtils.addAll(null, Arrays.asList("x")));
        Assert.assertEquals(0, CollectionUtils.addAll(target, null));
    }
}
