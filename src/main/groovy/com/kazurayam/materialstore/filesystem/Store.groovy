package com.kazurayam.materialstore.filesystem


import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.QueryOnMetadata

import java.awt.image.BufferedImage
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.time.temporal.TemporalUnit

/**
 * defines the public interface of the Store object
 */
interface Store {

    static final Store NULL_OBJECT = new StoreImpl(Files.createTempDirectory("TempDirectory"))

    int deleteMaterialsOlderThanExclusive(JobName jobName, JobTimestamp jobTimestamp,
                                          long amountToSubtract, TemporalUnit unit)

    List<JobTimestamp> findAllJobTimestamps(JobName jobName)

    List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName, JobTimestamp jobTimestamp)

    JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp)

    JobTimestamp findLatestJobTimestamp(JobName jobName)

    List<JobTimestamp> queryAllJobTimestamps(JobName jobName, QueryOnMetadata query)

    List<JobTimestamp> queryAllJobTimestampsPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp)

    JobTimestamp queryJobTimestampPriorTo(JobName jobName, QueryOnMetadata query, JobTimestamp jobTimestamp)

    JobTimestamp queryLatestJobTimestamp(JobName jobName, QueryOnMetadata query)

    Jobber getJobber(JobName jobName, JobTimestamp jobTimestamp)

    Path getPathOf(Material material)

    Path getRoot()

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                        QueryOnMetadata query)

    MaterialList select(JobName jobName, JobTimestamp jobTimestamp,
                        QueryOnMetadata query, FileType fileType)

    File selectFile(JobName jobName, JobTimestamp jobTimestamp,
                    QueryOnMetadata query, FileType fileType)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, BufferedImage input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, byte[] input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, File input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, Path input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input)

    Material write(JobName jobName, JobTimestamp jobTimestamp,
                   FileType fileType, Metadata meta, String input,
                   Charset charset)
}