package com.kazurayam.materialstore.differ


import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.FileTypeDiffability
import com.kazurayam.materialstore.filesystem.Jobber
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.metadata.Metadata

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

abstract class AbstractTextDiffer implements Differ {
    private Path root_

    private Charset charset = StandardCharsets.UTF_8

    AbstractTextDiffer() {}

    AbstractTextDiffer(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    @Override
    void setRoot(Path root) {
        ensureRoot(root)
        this.root_ = root
    }

    private static void ensureRoot(Path root) {
        Objects.requireNonNull(root)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
    }

    void setCharset(Charset chs) {
        Objects.requireNonNull(chs)
        this.charset = chs
    }

    @Override
    Artifact makeArtifact(Artifact artifact) {
        Objects.requireNonNull(root_)
        Objects.requireNonNull(artifact)
        Objects.requireNonNull(artifact.getLeft())
        Objects.requireNonNull(artifact.getRight())
        //
        Material left = artifact.getLeft()
        if (! left.getDiffability() == FileTypeDiffability.AS_TEXT) {
            throw new IllegalArgumentException("${left} is not a text")
        }
        Material right = artifact.getRight()
        if (! right.getDiffability() == FileTypeDiffability.AS_TEXT) {
            throw new IllegalArgumentException("${right} is not a text")
        }

        //
        TextDiffContent textDiffContent = makeContent(root_, left, right, charset)
        Double diffRatio = textDiffContent.getDiffRatio()

        //
        byte[] diffData = toByteArray(textDiffContent.getContent())
        Metadata diffMetadata = Metadata.builderWithMap([
                "category": "diff",
                "left": left.getIndexEntry().getID().toString(),
                "right": right.getIndexEntry().getID().toString(),
                "ratio": DifferUtil.formatDiffRatioAsString(diffRatio)])
                .build()
        Jobber jobber = new Jobber(root_, right.getJobName(), artifact.getDiffTimestamp())
        Material diffMaterial = jobber.write(diffData, FileType.HTML, diffMetadata, Jobber.DuplicationHandling.CONTINUE)
        //
        //
        Artifact result = new Artifact(artifact)
        result.setDiff(diffMaterial)
        result.setDiffRatio(diffRatio)
        return result
    }

    abstract TextDiffContent makeContent(Path root, Material original, Material revised, Charset charset)

    static List<String> readAllLines(String longText) {
        BufferedReader br = new BufferedReader(new StringReader(longText))
        List<String> lines = new ArrayList<>()
        String line
        while ((line = br.readLine()) != null) {
            lines.add(line)
        }
        return lines
    }

    static String readMaterial(Path root, Material material, Charset charset) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(material)
        Objects.requireNonNull(charset)
        if (material != Material.NULL_OBJECT) {
            Jobber jobber = new Jobber(root, material.getJobName(), material.getJobTimestamp())
            byte[] data = jobber.read(material.getIndexEntry())
            return new String(data, charset)
        } else {
            return ""
        }
    }

    private static byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8)
    }
}
