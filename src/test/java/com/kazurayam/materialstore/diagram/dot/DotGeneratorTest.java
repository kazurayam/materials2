package com.kazurayam.materialstore.diagram.dot;

import com.kazurayam.materialstore.zest.TestOutputOrganizerFactory;
import com.kazurayam.materialstore.base.inspector.Inspector;
import com.kazurayam.materialstore.base.reduce.MaterialProductGroup;
import com.kazurayam.materialstore.base.reduce.zipper.MaterialProduct;
import com.kazurayam.materialstore.core.FileType;
import com.kazurayam.materialstore.core.JobName;
import com.kazurayam.materialstore.core.JobTimestamp;
import com.kazurayam.materialstore.core.Material;
import com.kazurayam.materialstore.core.MaterialList;
import com.kazurayam.materialstore.core.MaterialstoreException;
import com.kazurayam.materialstore.core.Metadata;
import com.kazurayam.materialstore.core.Store;
import com.kazurayam.materialstore.core.Stores;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test takes is tentatively disabled
 * because it takes long time (over 1 minutes 20 seconds)
 */
@Disabled  // this test took too long time
public class DotGeneratorTest {

    private static final TestOutputOrganizer too =
            TestOutputOrganizerFactory.create(DotGeneratorTest.class);
    private static final Path issue259Dir =
            too.getProjectDirectory().resolve("src/test/fixtures/issue#259");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp leftTimestamp;
    private static JobTimestamp rightTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path root = too.cleanClassOutputDirectory().resolve("store");
        store = Stores.newInstance(root);
        // copy a fixture into the store
        too.copyDir(issue259Dir.resolve("store"), store.getRoot());
        jobName = new JobName("Main_Twins");
        leftTimestamp = new JobTimestamp("20220522_094639");
        rightTimestamp = new JobTimestamp("20220522_094706");
    }

    //@Disabled
    @Test
    public void test_generateDot_Material() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220522_094639");
        Material material = store.selectSingle(jobName, fixtureTimestamp, FileType.PNG);
        //
        String dotText = DotGenerator.generateDot(material, true);
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        //
        JobName outJobName = new JobName("test_generateDot_Material");
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toPath().toFile().length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toPath().toFile().length() > 0);
    }

    //@Disabled
    @Test
    public void test_generateDot_MaterialList() throws MaterialstoreException {
        JobTimestamp fixtureTimestamp = new JobTimestamp("20220522_094639");
        MaterialList materialList = store.select(jobName, fixtureTimestamp);
        //
        String dotText = DotGenerator.generateDot(materialList);
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        //
        JobName outJobName = new JobName("test_generateDot_MaterialList");
        JobTimestamp outJobTimestamp = JobTimestamp.now();
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toPath().toFile().length() > 0);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toPath().toFile().length() > 0);
    }

    //@Disabled
    @Test
    public void test_generateDot_MaterialProduct() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_generateDot_MaterialProduct");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProduct mp =
                new MaterialProduct.Builder(
                        leftMaterialList.get(0),
                        rightMaterialList.get(0),
                        jobName,
                        reducedTimestamp)
                        .build();
        //
        String dotText = DotGenerator.generateDot(mp);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toPath().toFile().length() > 0);
        //
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toPath().toFile().length() > 0);
    }

    //@Disabled
    @Test
    public void test_generateDotOfMPGBeforeZip() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_generateDotOfMPGBeforeZip");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("environment", "URL.host").build();
        //
        String dotText = DotGenerator.generateDotOfMPGBeforeZip(mProductGroup);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toPath().toFile().length() > 0);
        //
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toPath().toFile().length() > 0);
    }

    /**
     * Reproducing the issue of
     * https://github.com/kazurayam/VisualInspectionOfExcelAndPDF/issues/11
     * where either of the left or the right MaterialList is empty,
     * the DotGenerator.generateDotOfMPGBeforeZip(DotGenerator.java:203) raises
     * an IndexOutOfBoundsException: Index: 0, Size: 0
     */
    @Test
    public void test_generateDotOfMPGBeforeZip_withEmptyMaterialList() throws MaterialstoreException {
        JobTimestamp modifiedTimestamp = new JobTimestamp("20220522_000000");
        MaterialList leftMaterialList = store.select(jobName, modifiedTimestamp); // this will be emtpy
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        JobName outJobName = new JobName("test_generateDotOfMPGBeforeZip_withEmptyMaterialList");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup mProductGroup =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList).ignoreKeys("environment", "URL.host").build();
        //
        String dotText = DotGenerator.generateDotOfMPGBeforeZip(mProductGroup);
        assertNotNull(dotText);
    }

    //@Disabled
    @Test
    public void test_generateDot_MProductGroup() throws MaterialstoreException {
        MaterialList leftMaterialList = store.select(jobName, leftTimestamp);
        MaterialList rightMaterialList = store.select(jobName, rightTimestamp);
        JobTimestamp reducedTimestamp = JobTimestamp.now();
        // save the dot file and the PNG image into the store directory
        JobName outJobName = new JobName("test_generateDot_MProductGroup");
        JobTimestamp outJobTimestamp = JobTimestamp.laterThan(reducedTimestamp);
        MaterialProductGroup reduced =
                new MaterialProductGroup.Builder(
                        leftMaterialList,
                        rightMaterialList)
                        .ignoreKeys("environment", "URL.host")
                        .identifyWithRegex(
                                Collections.singletonMap("URL.query", "\\w{32}")
                        )
                        .build();
        assert reduced.size() > 0;
        //
        Inspector inspector = Inspector.newInstance(store);
        MaterialProductGroup inspected = inspector.reduceAndSort(reduced);

        // generate a dot file
        String dotText = DotGenerator.generateDot(inspected);
        Material dotMat =
                store.write(outJobName, outJobTimestamp, FileType.DOT,
                        Metadata.NULL_OBJECT, dotText);
        assertTrue(dotMat.toPath().toFile().length() > 0);
        // generate the image of the MProductGroup object by Graphviz
        BufferedImage bufferedImage = DotGenerator.toImage(dotText);
        Material pngMat =
                store.write(outJobName, outJobTimestamp, FileType.PNG,
                        Metadata.NULL_OBJECT, bufferedImage);
        assertTrue(pngMat.toPath().toFile().length() > 0);
    }
}
