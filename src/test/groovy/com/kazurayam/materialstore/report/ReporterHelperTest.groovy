package com.kazurayam.materialstore.report

import com.kazurayam.materialstore.filesystem.JobName
import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class ReporterHelperTest {

    @Test
    void test_getStyleFromClasspath() {
        String style = ReporterHelper.loadStyleFromClasspath() // https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
        assertNotNull(style)
        //println style
        assertTrue(style.length() > 0)
    }


}
