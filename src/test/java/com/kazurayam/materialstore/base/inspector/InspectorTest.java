package com.kazurayam.materialstore.base.inspector;

import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.core.filesystem.ID;
import com.kazurayam.materialstore.core.filesystem.JobName;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.Store;
import com.kazurayam.materialstore.core.filesystem.Stores;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InspectorTest {

    private static final Path outputDir = Paths.get(".").resolve("build/tmp/testOutput").resolve(InspectorTest.class.getName());
    private static final Path storeDir = outputDir.resolve("store");
    private static final Path issue80Dir = Paths.get(".").resolve("src/test/fixtures/issue#80");

    private Store store;
    private MaterialList left;
    private MaterialList right;
    private Inspector inspector;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }

        Files.createDirectories(outputDir);
        FileUtils.copyDirectory(issue80Dir.toFile(), storeDir.toFile());
    }

    @BeforeEach
    public void before() throws MaterialstoreException {
        store = Stores.newInstance(storeDir);
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp timestampP = new JobTimestamp("20220128_191320");
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("environment", "MyAdmin_ProductionEnv");
        left = store.select(jobName, timestampP, QueryOnMetadata.builder(map).build());
        JobTimestamp timestampD = new JobTimestamp("20220128_191342");
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>(1);
        map1.put("environment", "MyAdmin_DevelopmentEnv");
        right = store.select(jobName, timestampD, QueryOnMetadata.builder(map1).build());
        inspector = Inspector.newInstance(store);
    }

    @Test
    public void test_reduceAndSort() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup reducedMPG = MaterialProductGroup.builder(left, right).ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol").identifyWithRegex(map).sort("URL.host").build();
        Assertions.assertNotNull(reducedMPG);

        MaterialProductGroup processedMPG = inspector.reduceAndSort(reducedMPG);
        Assertions.assertNotNull(processedMPG);

        processedMPG.forEach(mProduct -> Assertions.assertNotEquals(ID.NULL_OBJECT,
                mProduct.getDiff().getIndexEntry().getID()));
        assertEquals(8, processedMPG.size());
    }

    @Test
    public void test_report_MaterialList() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp jobTimestamp = new JobTimestamp("20220128_191320");
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        //System.out.println("materialList=" + materialList.toTemplateModel());
        Inspector inspector = Inspector.newInstance(store);
        Path report = inspector.report(materialList);
        Assertions.assertNotNull(report);
        Assertions.assertTrue(Files.exists(report));
    }

    @Test
    public void test_report_MaterialProductGroup() throws MaterialstoreException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("URL.query", "\\w{32}");
        MaterialProductGroup reducedMPG = MaterialProductGroup.builder(left, right)
                .ignoreKeys("environment", "URL.host", "URL.port", "URL.protocol")
                .identifyWithRegex(map)
                .sort("URL.host")
                .build();
        MaterialProductGroup processedMPG = inspector.reduceAndSort(reducedMPG);
        double threshold = 0.0D;
        Assertions.assertTrue(processedMPG.countWarnings(threshold) > 0);

        Path report = inspector.report(processedMPG, threshold);
        Assertions.assertNotNull(report);
        Assertions.assertTrue(Files.exists(report));
    }

    @Test
    public void test_resolveReportFileName_MaterialList() throws MaterialstoreException {
        JobName jobName = new JobName("MyAdmin_visual_inspection_twins");
        JobTimestamp jobTimestamp = new JobTimestamp("20220128_191320");
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        String fileName = inspector.resolveReportFileName(materialList);
        assertEquals(jobName.toString() + "-" +
                        jobTimestamp.toString() + ".html",
                fileName);
    }

    @Test
    public void test_resolveReportFileName_MaterialProductGroup() {
        MaterialProductGroup mpg = MaterialProductGroup.builder(left, right)
                .build();
        String fileName = inspector.resolveReportFileName(mpg);
        assertEquals(mpg.getJobName() + "-" +
                        mpg.getJobTimestampOfReduceResult().toString() +
                        ".html",
                fileName);
    }
}