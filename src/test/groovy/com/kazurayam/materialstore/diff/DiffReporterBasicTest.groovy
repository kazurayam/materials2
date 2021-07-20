package com.kazurayam.materialstore.diff

import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.JobTimestamp
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.MetadataPattern
import com.kazurayam.materialstore.store.StoreImpl
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DiffReporterBasicTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DiffReporterBasicTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
    }

    @Test
    void test_reportDiffs_PNG_and_HTML() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        assert Files.exists(root)
        JobName jobName = new JobName("test_reportDiffs_PNG_and_HTML")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        //
        List<Material> expected = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "ProductionEnv"]))

        List<Material> actual = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern(["profile": "DevelopmentEnv"]))

        List<DiffArtifact> input =
                storeImpl.zipMaterials(expected, actual, ["URL.file", "category"] as Set)
        //
        DifferDriver differDriver = new DifferDriverImpl.Builder().root(root).build()
        List<DiffArtifact> stuffed = differDriver.makeDiff(input)
        assertNotNull(stuffed)
        assertEquals(2, stuffed.size())
        //
        Path reportFile = root.resolve("report.md")
        DiffReporter diffReporter = new DiffReporterBasic(root)
        diffReporter.reportDiffs(stuffed, reportFile)
        //
        assertTrue(Files.exists(reportFile))
        assertTrue(reportFile.toFile().length() > 0)
    }


}