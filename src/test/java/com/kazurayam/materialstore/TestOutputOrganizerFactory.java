package com.kazurayam.materialstore;

import com.kazurayam.unittest.TestOutputOrganizer;

public class TestOutputOrganizerFactory {
    
    private TestOutputOrganizerFactory() {}

    public static TestOutputOrganizer create(Class<?> clazz) {
        return new TestOutputOrganizer.Builder(clazz)
                .outputDirPath("build/tmp/testOutput")
                .subDirPath(clazz.getName())
                // e.g, "io.github.somebody.somestuff.SampleTest"
                .build();
    }
}
