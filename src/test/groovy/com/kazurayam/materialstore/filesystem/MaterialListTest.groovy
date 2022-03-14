package com.kazurayam.materialstore.filesystem


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class MaterialListTest {

    private final String sampleLine = """6141b40cfe9e7340a483a3097c4f6ff5d20e04ea\tpng\t{"profile":"DevelopmentEnv","URL":"http://demoaut-mimic.kazurayam.com/"}"""

    private JobName jobName = new JobName("MaterialListTest")
    private QueryOnMetadata query = QueryOnMetadata.ANY
    private Material material

    @BeforeEach
    void beforeEach() {
        IndexEntry indexEntry = IndexEntry.parseLine(sampleLine)
        material = new Material(JobName.NULL_OBJECT, JobTimestamp.NULL_OBJECT, indexEntry)
    }

    @Test
    void test_countMaterialsWithIdStartingWith() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        assertEquals(0, materialList.countMaterialsWithIdStartingWith("6141b40"))
        materialList.add(material)
        assertEquals(1, materialList.countMaterialsWithIdStartingWith("6141b40"))
    }

    @Test
    void test_smoke() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        materialList.add(material)
        assertEquals(1, materialList.size())
        assertTrue(materialList.contains(material))
    }

    @Test
    void test_getQueryOnMetadata() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        materialList.add(material)
        assertNotNull(materialList.getQueryOnMetadata())
        //println materialList.getQueryOnMetadata().toString()
    }


    @Test
    void test_getJobName() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        assertEquals(jobName, materialList.getJobName())
    }

    @Test
    void test_getJobTimestamp() {
        JobTimestamp now = JobTimestamp.now()
        MaterialList materialList = new MaterialList(jobName, now, query)
        assertEquals(now, materialList.getJobTimestamp())
    }

    @Test
    void test_toString() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        materialList.add(material)
        String str = materialList.toString()
        assertNotNull(str)
        //println str
    }

    @Test
    void test_toJson() {
        MaterialList materialList = new MaterialList(jobName, JobTimestamp.now(), query)
        materialList.add(material)
        String json = materialList.toJson()
        assertNotNull(json)
        println json
    }
}
