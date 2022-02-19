package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.StoreImpl
import com.kazurayam.materialstore.TestFixtureUtil
import com.kazurayam.materialstore.diffartifact.DiffArtifactGroup
import com.kazurayam.materialstore.metadata.IgnoringMetadataKeys
import com.kazurayam.materialstore.metadata.MetadataPattern
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class DifferDriverImplTest {

    private static Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(DifferDriverImplTest.class.getName())

    private static Path resultsDir =
            Paths.get(".").resolve("src/test/resources/fixture/sample_results")

    private static Path root
    private static Store store

    @BeforeAll
    static void beforeAll() {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile())
        }
        Files.createDirectories(outputDir)
        root = outputDir.resolve("store")
        Files.createDirectories(root)
        store = new StoreImpl(root)
        assert Files.exists(root)
    }

    @Test
    void test_Builder_differFor() {
        DifferDriver differDriver = new DifferDriverImpl.Builder(root)
                .differFor(FileType.JPEG, new ImageDifferToPNG())
                .build()
        assertTrue(differDriver.hasDiffer(FileType.JPEG))
    }

    @Test
    void test_TextDiffer() {
        JobName jobName = new JobName("test_TextDiffer")
        TestFixtureUtil.setupFixture(store, jobName)

        JobTimestamp timestamp1 = new JobTimestamp("20210715_145922")
        MaterialList left = store.select(jobName, timestamp1,
                MetadataPattern.builderWithMap(["profile": "ProductionEnv"]).build(),
                FileType.HTML)
        assertEquals(1, left.size())

        JobTimestamp timestamp2 = new JobTimestamp("20210715_145922")
        MaterialList right = store.select(jobName, timestamp2,
                MetadataPattern.builderWithMap(["profile": "DevelopmentEnv"]).build(),
                FileType.HTML)
        assertEquals(1, right.size())

        DiffArtifactGroup diffArtifactGroup =
                DiffArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        assertNotNull(diffArtifactGroup)
        assertEquals(1, diffArtifactGroup.size())
        //
        DifferDriver differDriver = new DifferDriverImpl.Builder(store).build()
        diffArtifactGroup.applyResolvent(differDriver)
        assertEquals(1, diffArtifactGroup.size())
    }

    @Test
    void test_ImageDiffer() {
        assert Files.exists(root)
        JobName jobName = new JobName("test_ImageDiffer")
        TestFixtureUtil.setupFixture(store, jobName)
        JobTimestamp jobTimestamp = new JobTimestamp("20210715_145922")
        //
        MaterialList left = store.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap(["profile": "ProductionEnv"]).build(),
                FileType.PNG)

        MaterialList right = store.select(jobName, jobTimestamp,
                MetadataPattern.builderWithMap(["profile": "DevelopmentEnv"]).build(),
                FileType.PNG)

        DiffArtifactGroup diffArtifactGroup =
                DiffArtifactGroup.builder(left, right)
                        .ignoreKeys("profile", "URL", "URL.host")
                        .build()
        assertNotNull(diffArtifactGroup)
        assertEquals(2, diffArtifactGroup.size())
        //
        DifferDriver differDriver = new DifferDriverImpl.Builder(store).build()
        diffArtifactGroup.applyResolvent(differDriver)
        assertNotNull(diffArtifactGroup)
        assertEquals(2, diffArtifactGroup.size())
    }

}
