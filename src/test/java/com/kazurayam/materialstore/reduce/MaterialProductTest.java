package com.kazurayam.materialstore.reduce;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Jobber;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.util.DotUtil;
import com.kazurayam.materialstore.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaterialProductTest {

    private static final Path outputDir = Paths.get(".")
            .resolve("build/tmp/testOutput")
            .resolve(MaterialProductTest.class.getName());

    private static final Path fixtureDir = Paths.get(".")
            .resolve("src/test/fixture/issue#73/MyAdmin_visual_inspection_twins");

    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
        //

    }

    @Test
    public void test_getDescription_more() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(3);
        map.put("URL.path", "/");
        map.put("profile", "Flaskr_ProductionEnv");
        map.put("step", "6");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        SortKeys sortKeys = new SortKeys("step", "profile");
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).sortKeys(sortKeys).build();
        String description = mProduct.getDescription();
        assertEquals("{\"step\":\"6\", \"profile\":\"Flaskr_ProductionEnv\", \"URL.path\":\"/\"}", description);
    }

    @Test
    public void test_getDescription() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        assertEquals(
                "{\"URL.file\":\"/\", \"URL.host\":\"demoaut-mimic.kazurayam.com\"}",
                mProduct.getDescription());
    }

    @Test
    public void test_toString() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        System.out.println(JsonUtil.prettyPrint(mProduct.toString()));
    }

    @Test
    public void test_toJson() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("URL.host", "demoaut-mimic.kazurayam.com");
        map.put("URL.file", "/");
        QueryOnMetadata mp = QueryOnMetadata.builder(map).build();
        MaterialProduct mProduct = new MaterialProduct.Builder(Material.NULL_OBJECT, Material.NULL_OBJECT, JobTimestamp.now()).setQueryOnMetadata(mp).build();
        String json = mProduct.toJson(true);
        System.out.println(json);
    }

    @Test
    public void test_toDot() throws IOException, MaterialstoreException, InterruptedException {
        JobName jobName = new JobName("test_toDot");
        // stuff the Job directory with a fixture
        Path jobNameDir = store.getRoot().resolve(jobName.toString());
        FileUtils.copyDirectory(fixtureDir.toFile(), jobNameDir.toFile());
        //
        Jobber jobberOfLeft = store.getJobber(jobName, new JobTimestamp("20220125_140449"));
        MaterialList leftList = jobberOfLeft.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "MyAdmin_ProductionEnv")
                        .put("URL.path", Pattern.compile(".*")).build(),
                FileType.JS);
        assertEquals(2, leftList.size());
        //
        Jobber jobberOfRight = store.getJobber(jobName, new JobTimestamp("20220125_140509"));
        MaterialList rightList = jobberOfRight.selectMaterials(
                QueryOnMetadata.builder()
                        .put("profile", "MyAdmin_DevelopmentEnv")
                        .put("URL.path", Pattern.compile(".*")).build(),
                FileType.JS);
        assertEquals(2, rightList.size());

        // create a MProductGroup object
        MProductGroup mProductGroup =
                MProductGroup.builder(leftList, rightList)
                        .ignoreKeys("profile", "URL", "URL.host", "category")
                        .build();
        Assertions.assertNotNull(mProductGroup);
        assertEquals(2, mProductGroup.size());

        JobTimestamp jobTimestamp = JobTimestamp.now();
        // check the 1st MProduct object
        MaterialProduct mp1 = mProductGroup.get(0);
        String dot1 = mp1.toDot();
        Metadata metadata1 = Metadata.builder().put("seq", "1").build();
        store.write(jobName, jobTimestamp, FileType.DOT, metadata1, dot1);
        DotUtil.storeDiagram(store, jobName, jobTimestamp, metadata1, dot1);
    }

    @Test
    public void test_toDot_NULL_OBJECT() throws IOException, MaterialstoreException, InterruptedException {
        String dot = MaterialProduct.NULL_OBJECT.toDot();
        JobName jobName = new JobName("test_toDot_NULL_OBJECT");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata = Metadata.builder().build();
        store.write(jobName, jobTimestamp, FileType.DOT, metadata, dot);
        DotUtil.storeDiagram(store, jobName, jobTimestamp, metadata, dot);
    }
}
