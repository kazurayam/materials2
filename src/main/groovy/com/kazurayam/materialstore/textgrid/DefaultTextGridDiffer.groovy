package com.kazurayam.materialstore.textgrid

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

import com.kazurayam.materialstore.FileType
import com.kazurayam.materialstore.JobName
import com.kazurayam.materialstore.JobTimestamp
import com.kazurayam.materialstore.Material
import com.kazurayam.materialstore.Metadata
import com.kazurayam.materialstore.Store

class DefaultTextGridDiffer extends TextGridDifferBuilder {

    DefaultTextGridDiffer() {
        super()
    }

    DefaultTextGridDiffer(Path projectDir) {
        super(projectDir)
    }

    /**
     *
     */
    @Override
    void jsonifyAndStoreRows(Store store, JobName jobName, JobTimestamp jobTimestamp,
                             List<List<String>> input, String inputId) {
        List<String> lines = input.stream()
                .map({ List<String> list -> new Row(list, 0..0) })
                .map({ Row row -> row.values().toJson() })
                .collect(Collectors.toList())
        //
        Path tempFile = Files.createTempFile(null, null)
        writeLinesIntoFile(lines, tempFile.toFile())
        Metadata metadata = new Metadata.Builder()
                .put("input", inputId)
                .put("target", "rows")
                .build()
        Material mat = store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile)
    }

    @Override
    void jsonifyAndStoreKeys(Store store, JobName jobName, JobTimestamp jobTimestamp,
                             List<List<String>> input, String inputId) {
        Set<String> keys = input.stream()
                .map({ List<String> list -> new Row(list, 0..1) })
                .map({ Row row -> row.key().toJson() })
                .collect(Collectors.toSet() )
        List<String> lines = new ArrayList<>(keys)
        Collections.sort(lines)
        //
        Path tempFile = Files.createTempFile(null, null)
        writeLinesIntoFile(lines, tempFile.toFile())
        Metadata metadata = new Metadata.Builder()
                .put("input", inputId)
                .put("target", "keys")
                .build()
        Material mat = store.write(jobName, jobTimestamp, FileType.JSON, metadata, tempFile)
    }
}