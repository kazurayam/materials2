package com.kazurayam.materialstore.reduce


import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Material
import com.kazurayam.materialstore.filesystem.TemplateReady
import com.kazurayam.materialstore.filesystem.metadata.IdentifyMetadataValues
import com.kazurayam.materialstore.filesystem.metadata.IgnoreMetadataKeys
import com.kazurayam.materialstore.reduce.differ.DifferUtil
import com.kazurayam.materialstore.filesystem.QueryOnMetadata
import com.kazurayam.materialstore.filesystem.metadata.SortKeys
import com.kazurayam.materialstore.util.JsonUtil

/**
 * "Material x Material" = "Materials Product"
 *
 * is used to carry data of a pair of "Material" objects,
 * plus the "diff" of the two.
 *
 */
final class MaterialProduct implements Comparable, TemplateReady {

    public static final MaterialProduct NULL_OBJECT =
            new Builder(Material.NULL_OBJECT, Material.NULL_OBJECT,
                    JobTimestamp.NULL_OBJECT)
                    .setQueryOnMetadata(QueryOnMetadata.NULL_OBJECT)
                    .build()

    private final Material left
    private final Material right
    private final JobTimestamp reducedTimestamp
    private final QueryOnMetadata query
    private final SortKeys sortKeys
    //
    private Material diff
    private Double diffRatio

    private MaterialProduct(Builder builder) {
        this.left = builder.left
        this.right = builder.right
        this.diff = builder.diff
        this.reducedTimestamp = builder.reducedTimestamp
        this.query = builder.query
        this.diffRatio = 0.0d
        this.sortKeys = builder.sortKeys
    }

    /**
     * copy constructor
     *
     * @param source
     */
    MaterialProduct(MaterialProduct source) {
        Objects.requireNonNull(source)
        this.left = source.getLeft()
        this.right = source.getRight()
        this.diff = source.getDiff()
        this.reducedTimestamp = source.reducedTimestamp
        this.query = source.getQueryOnMetadata()
        this.sortKeys = source.getSortKeys()
    }

    void annotate(IgnoreMetadataKeys ignoreMetadataKeys,
                  IdentifyMetadataValues identifyMetadataValues) {
        this.left.getMetadata().annotate(query, ignoreMetadataKeys, identifyMetadataValues)
        this.right.getMetadata().annotate(query, ignoreMetadataKeys, identifyMetadataValues)
    }

    void setDiff(Material diff) {
        Objects.requireNonNull(diff)
        this.diff = diff
    }

    void setDiffRatio(Double diffRatio) {
        Objects.requireNonNull(diffRatio)
        this.diffRatio = diffRatio
    }

    Material getLeft() {
        return this.left
    }

    SortKeys getSortKeys() {
        return this.sortKeys
    }

    String getFileTypeExtension() {
        if (this.getLeft() == Material.NULL_OBJECT) {
            return this.getRight().getIndexEntry().getFileType().getExtension()
        } else {
            return this.getLeft().getIndexEntry().getFileType().getExtension()
        }
    }

    Material getRight() {
        return this.right
    }

    Material getDiff() {
        return this.diff
    }

    Double getDiffRatio() {
        return this.diffRatio
    }

    String getDiffRatioAsString() {
        return DifferUtil.formatDiffRatioAsString(this.getDiffRatio())
    }

    JobTimestamp getReducedTimestamp() {
        return this.reducedTimestamp
    }

    QueryOnMetadata getQueryOnMetadata() {
        return this.query
    }

    /**
     * String representation of this MaterialProduct instance
     *
     * @return
     */
    String getDescription() {
        return this.query.getDescription(sortKeys)
    }

    @Override
    boolean equals(Object obj) {
        if (! obj instanceof MaterialProduct) {
            return false
        }
        MaterialProduct other = (MaterialProduct)obj
        return this.getRight() == other.getRight() &&
                this.getLeft() == other.getLeft()
    }

    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getLeft().hashCode()
        hash = 31 * hash + this.getRight().hashCode()
        if (this.getDiff() != null) {
            hash = 31 * hash + this.getDiff().hashCode()
        }
        return hash
    }

    @Override
    String toString() {
        return toJson()
    }


    @Override
    int compareTo(Object obj) {
        if (! obj instanceof MaterialProduct) {
            throw new IllegalArgumentException("obj is not instance of DiffResult")
        }
        MaterialProduct other = (MaterialProduct)obj

        // Note that the SortKey is taken into account here indirectly
        return this.getDescription() <=> other.getDescription()
    }

    //--------Jsonifiable----------------------------------------------
    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"reducedTimestamp\":\"")
        sb.append(reducedTimestamp.toString())
        sb.append("\",")
        sb.append("\"diffRatio\":")
        sb.append(diffRatio)
        sb.append(",")
        sb.append("\"fileTypeExtension\":")
        sb.append("\"" + getFileTypeExtension() + "\"")
        sb.append(",")
        sb.append("\"queryOnMetadata\":")
        sb.append(query.toJson())
        sb.append(",")
        sb.append("\"description\":")
        sb.append("\"" + JsonUtil.escapeAsJsonString(query.toJson()) + "\"")
        sb.append(",")
        sb.append("\"left\":")
        sb.append(left.toJson())
        sb.append(",")
        sb.append("\"right\":")
        sb.append(right.toJson())
        sb.append(",")
        sb.append("\"diff\":")
        sb.append(diff.toJson())
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }


    /**
     *
     */
    static class Builder {
        // required
        private Material left
        private Material right
        private JobTimestamp reducedTimestamp
        // optional
        private Material diff
        private QueryOnMetadata query
        private Double diffRatio
        private SortKeys sortKeys
        Builder(Material left, Material right, JobTimestamp reducedTimestamp) {
            Objects.requireNonNull(left)
            Objects.requireNonNull(right)
            Objects.requireNonNull(reducedTimestamp)
            this.left = left
            this.right = right
            this.reducedTimestamp = reducedTimestamp
            this.diff = Material.NULL_OBJECT
            this.query = QueryOnMetadata.NULL_OBJECT
            this.diffRatio = -1.0d
            this.sortKeys = SortKeys.NULL_OBJECT
        }
        Builder setQueryOnMetadata(QueryOnMetadata query) {
            Objects.requireNonNull(query)
            this.query = query
            return this
        }
        Builder sortKeys(SortKeys sortKeys) {
            this.sortKeys = sortKeys
            return this
        }
        MaterialProduct build() {
            return new MaterialProduct(this)
        }
    }
}