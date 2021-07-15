package com.kazurayam.materials.store

import com.kazurayam.materials.MaterialsException
import com.kazurayam.materials.diff.DiffArtifact

import java.nio.file.Files
import java.nio.file.Path

class Jobber {

    private final JobName jobName
    private final JobTimestamp jobTimestamp
    private final Path jobResultDir

    private final Index index

    Jobber(Path root, JobName jobName, JobTimestamp jobTimestamp) {
        this.jobName = jobName
        this.jobTimestamp = jobTimestamp
        jobResultDir = root.resolve(jobName.toString()).resolve(jobTimestamp.toString())
        Files.createDirectories(getObjectsDir())

        // the content of "index" is cached in memory
        index = new Index()
        Path indexFile = Index.getIndexFile(jobResultDir)
        if (Files.exists(indexFile)) {
            index.deserialize(indexFile)
        }
    }

    Path getJobResultDir() {
        return jobResultDir
    }

    JobName getJobName() {
        return jobName
    }

    JobTimestamp getJobTimestamp() {
        return jobTimestamp
    }

    Path getObjectsDir() {
        return getJobResultDir().resolve("objects")
    }

    /**
     * This "commit" method is the most significant operation in the TAOD project.
     *
     * Each MObject is identified by its FileType + Metadata combination.
     * Jobber ensures that MObject under the "object" is unique by FileType + Metadata.
     * If you try to commit a MObject with duplicating FileType + Metadata, the commit
     * will be rejected.
     *
     * @param metadata
     * @param data
     * @param fileType
     * @return Material
     */
    Material commit(byte[] data, FileType fileType, Metadata metadata)
            throws MaterialsException {
        Objects.requireNonNull(metadata)
        if (data.length == 0 ) throw new IllegalArgumentException("length of the data is 0")
        Objects.requireNonNull(fileType)

        MObject mObject = new MObject(data, fileType)

        // check if the MObject is already there.
        if (mObject.exists(this.getObjectsDir())) {
            throw new MaterialsException("fileType=${fileType} metadata=${metadata}:" +
                    " MObject is already in the Store." +
                    " Metadata is duplicating." +
                    " Give more detailed metadata to make this object uniquely identifiable.")
        }

        // save the "byte[] data" into disk
        Path objectFile = this.getObjectsDir().resolve(mObject.getFileName())
        mObject.serialize(objectFile)

        // insert a line into the "index" content on memory
        IndexEntry indexEntry = index.put(mObject.getID(), fileType, metadata)

        // save the content of "index" into disk everytime when a commit is made
        index.serialize(Index.getIndexFile(jobResultDir))

        return new Material(this.getJobName(), this.getJobTimestamp(), indexEntry)
    }

    /**
     *
     * @param fileType
     * @param metadataPattern
     * @return
     */
    List<Material> select(FileType fileType, MetadataPattern metadataPattern) {
        Objects.requireNonNull(fileType)
        Objects.requireNonNull(metadataPattern)
        List<Material> result = new ArrayList<Material>()
        index.eachWithIndex { IndexEntry entry, x ->
            if (entry.getFileType() == fileType &&
                    entry.getMetadata().match(metadataPattern)) {
                Material material = new Material(jobName, jobTimestamp, entry)
                result.add(material)
            }
        }
        return result
    }

}
