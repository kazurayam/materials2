package com.kazurayam.materialstore.textgrid


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.DiffArtifacts
import com.kazurayam.materialstore.IgnoringMetadataKeys
import com.kazurayam.materialstore.JobName
import com.kazurayam.materialstore.JobTimestamp
import com.kazurayam.materialstore.MaterialList
import com.kazurayam.materialstore.MetadataPattern
import com.kazurayam.materialstore.Store
import com.kazurayam.materialstore.Stores
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The "Builder" pattern of GOF is employed
 *
 */
abstract class TextGridDifferBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TextGridDifferBuilder.class)

    private static Path projectDir

    private Path reportFile

    TextGridDifferBuilder() {
        this(Paths.get(System.getProperty("user.dir")))
    }

    TextGridDifferBuilder(Path projectDir) {
        this.projectDir = projectDir
        this.reportFile = null
    }

    /*
     *
     */
    int diffTextGrids(List<List<String>> input1, List<List<String>> input2,
                          String givenJobName) {
        Path root = projectDir.resolve("store")
        Store store = Stores.newInstance(root)
        JobName jobName = new JobName(givenJobName)

        JobTimestamp timestamp1 = JobTimestamp.now()
        jsonifyAndStore(store, jobName, timestamp1, input1, "input1")

        Thread.sleep(1000)

        JobTimestamp timestamp2 = JobTimestamp.now()
        jsonifyAndStore(store, jobName, timestamp2, input2, "input2")

        MaterialList left = store.select(jobName, timestamp1,
                new MetadataPattern.Builder().build())

        MaterialList right = store.select(jobName, timestamp2,
                new MetadataPattern.Builder().build())
        double criteria = 0.0d

        DiffArtifacts stuffedDiffArtifacts = store.makeDiff(left, right, IgnoringMetadataKeys.of("input"))
        int warnings = stuffedDiffArtifacts.countWarnings(criteria)

        reportFile = store.reportDiffs(jobName, stuffedDiffArtifacts, criteria, jobName.toString() + "-index.html")
        assert Files.exists(reportFile)
        logger.info("report is found at " + reportFile.normalize().toAbsolutePath())

        return warnings
    }

    /*
     *
     */
    private final void jsonifyAndStore(Store store,
                                       JobName jobName, JobTimestamp jobTimestamp,
                                       List<List<String>> input, String inputId) {
        jsonifyAndStoreRows(store, jobName, jobTimestamp, input, inputId)
        jsonifyAndStoreKeys(store, jobName, jobTimestamp, input, inputId)
    }

    /*
     *
     */
    abstract void jsonifyAndStoreRows(
            Store store, JobName jobName, JobTimestamp jobTimestamp,
            List<List<String>> input, String inputId)

    /*
     *
     */
    abstract void jsonifyAndStoreKeys(
            Store store, JobName jobName, JobTimestamp jobTimestamp,
            List<List<String>> input, String inputId)


    /*
     *
     */
    Path getReportPath() {
        return reportFile.normalize().toAbsolutePath()
    }

    Path getReportPathRelativeTo(Path base) {
        return base.relativize(getReportPath())
    }

    // ---------- helpers

    protected static final void writeLinesIntoFile(List<String> lines, File file) {
        PrintWriter pw = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file),"UTF-8")))
        for (String line in lines) {
            pw.println(line)
        }
        pw.flush()
        pw.close()
    }

}