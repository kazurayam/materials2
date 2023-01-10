package com.kazurayam.materialstore;

import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFixtureSupportTest {

    private Path testClassOutputDir;
    private Store store;

    @BeforeEach
    public void setup() {
        testClassOutputDir = TestHelper.createTestClassOutputDir(TestFixtureSupportTest.class);
        store = Stores.newInstance(testClassOutputDir.resolve("store"));
    }

    @Test
    public void test_copyFixture() throws MaterialstoreException, IOException {
        Path fixtureDir = TestHelper.getFixturesDirectory().resolve("issue#331");
        TestHelper.copyDirectory(fixtureDir, testClassOutputDir);
        assertTrue(store.contains(new JobName("CURA")));
    }
}