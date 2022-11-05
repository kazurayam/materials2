package com.kazurayam.materialstore.core.filesystem.metadata;

import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.SortKeys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.kazurayam.materialstore.core.filesystem.Metadata;

public class MetadataImplTest {

    private static Map<String, MetadataAttribute> attributes;

    private static Metadata metadata;

    @BeforeAll
    public static void beforeAll() throws MalformedURLException {
        URL url = new URL("https://www.google.com/");
        JobTimestamp timestamp = JobTimestamp.now();
        metadata = new Metadata.Builder(url)
                .put("timestamp", timestamp.toString())
                .put("step","01")
                .build();
        attributes = new HashMap<String, MetadataAttribute>();
        attributes.put("step", new MetadataAttribute("step", "01"));
        attributes.put("URL.protocol", new MetadataAttribute("URL.protocol", "https"));
        attributes.put("URL.host", new MetadataAttribute("URL.host", "www.google.com"));
        attributes.put("URL.port", new MetadataAttribute("URL.port", "80"));
        attributes.put("URL.path", new MetadataAttribute("URL.path", "/"));
        attributes.put("timestamp", new MetadataAttribute("timestamp", timestamp.toString()));
    }

    @Test
    public void test_getSortedKeys_emptySortKeys() {
        SortKeys sortKeys = new SortKeys();
        List<String> sorted = MetadataImpl.getSortedKeys(attributes, sortKeys);
        assertEquals("step", sorted.get(0));
        assertEquals("timestamp", sorted.get(1));
        assertEquals("URL.host", sorted.get(2));
        assertEquals("URL.path", sorted.get(3));
        assertEquals("URL.port", sorted.get(4));
        assertEquals("URL.protocol", sorted.get(5));
    }

    /**
     * with a SortKeys instance as MetadataImpl.getSortedKeys(Attributes, SortKeys) call,
     * you can specify how the keys should be ordered in the returned string.
     */
    @Test
    public void test_getSortedKeys_withSortKeys() {
        SortKeys sortKeys = new SortKeys("timestamp", "step",
                "URL.protocol", "URL.host", "URL.port", "URL.path");
        List<String> sorted = MetadataImpl.getSortedKeys(attributes, sortKeys);
        assertEquals("timestamp", sorted.get(0));
        assertEquals("step", sorted.get(1));
        assertEquals("URL.protocol", sorted.get(2));
        assertEquals("URL.host", sorted.get(3));
        assertEquals("URL.port", sorted.get(4));
        assertEquals("URL.path", sorted.get(5));
        //System.out.println(sorted);
    }


    @Test
    public void test_getMetadataIdentification() {
        SortKeys sortKeys = new SortKeys("timestamp", "step");
        MetadataIdentification desc = metadata.getMetadataIdentification(sortKeys);
        assertTrue(desc.toString().startsWith("{\"timestamp\":"), desc.toString());
    }

}