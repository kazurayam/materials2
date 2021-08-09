package com.kazurayam.materialstore.differ

import com.kazurayam.materialstore.*
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ImageDifferToPNGTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(ImageDifferToPNGTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")


    @Test
    void test_makeDiff() {
        Path root = outputDir.resolve("Materials")
        StoreImpl storeImpl = new StoreImpl(root)
        JobName jobName = new JobName("test_makeDiff")
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        TestFixtureUtil.setupFixture(storeImpl, jobName)
        //
        List<Material> left = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "ProductionEnv"]).build(),
                FileType.PNG)

        List<Material> right = storeImpl.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(["profile": "DevelopmentEnv"]).build(),
                FileType.PNG)

        DiffArtifacts diffArtifacts =
                storeImpl.zipMaterials(left, right,
                        new MetadataIgnoredKeys.Builder()
                                .ignoreKey("profile")
                                .ignoreKey("URL")
                                .ignoreKey("URL.host")
                                .build())
        assertNotNull(diffArtifacts)
        assertEquals(2, diffArtifacts.size(), JsonOutput.prettyPrint(diffArtifacts.toString()))
        //
        DiffArtifact stuffed = new ImageDifferToPNG(root).makeDiffArtifact(diffArtifacts.get(0))
        assertNotNull(stuffed)
        assertNotNull(stuffed.getDiff())
        assertTrue(stuffed.getDiffRatio() > 0)
        assertNotEquals(Material.NULL_OBJECT, stuffed.getDiff())
    }
}