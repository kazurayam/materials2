package com.kazurayam.materialstore.base.reduce.zipper;

import com.kazurayam.materialstore.core.filesystem.IFileType;
import com.kazurayam.materialstore.core.filesystem.JobTimestamp;
import com.kazurayam.materialstore.core.filesystem.Material;
import com.kazurayam.materialstore.core.filesystem.MaterialList;
import com.kazurayam.materialstore.core.filesystem.Metadata;
import com.kazurayam.materialstore.core.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.core.filesystem.metadata.IdentifyMetadataValues;
import com.kazurayam.materialstore.core.filesystem.metadata.IgnoreMetadataKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * contains the "zipMaterials" method that takes 2 MaterialList objects (left, right)
 * together with some parameters, create a List of MaterialProduct objects.
 * 2 Lists are converted into 1 List.
 * It is the reason why this method and this class is named "zipper".
 */
public final class Zipper {

    private static Logger logger = LoggerFactory.getLogger(Zipper.class);

    private final IgnoreMetadataKeys ignoreMetadataKeys;
    private final IdentifyMetadataValues identifyMetadataValues;

    public Zipper(final IgnoreMetadataKeys ignoreMetadataKeys,
                  final IdentifyMetadataValues identifyMetadataValues) {
        Objects.requireNonNull(ignoreMetadataKeys);
        Objects.requireNonNull(identifyMetadataValues);
        this.ignoreMetadataKeys = ignoreMetadataKeys;
        this.identifyMetadataValues = identifyMetadataValues;
    }

    /**
     *
     */
    public List<MaterialProduct> zipMaterials(
            final MaterialList leftList,
            final MaterialList rightList,
            final JobTimestamp resultTimestamp) {
        Objects.requireNonNull(leftList);
        Objects.requireNonNull(rightList);
        Objects.requireNonNull(resultTimestamp);
        String methodName = "[zipMaterials] ";
        // the result
        final List<MaterialProduct> mProductList = new ArrayList<>();

        //
        Iterator<Material> rightIter = rightList.iterator();
        while (rightIter.hasNext()) {
            Material right = rightIter.next();
            final IFileType rightFileType = right.getIndexEntry().getFileType();
            Metadata rightMetadata = right.getIndexEntry().getMetadata();
            final QueryOnMetadata rightPattern =
                    QueryOnMetadata.builder(rightMetadata, ignoreMetadataKeys).build();
            //
            logger.debug(methodName + "--------");
            logger.debug(methodName + "right " + right.getShortID() + " "
                    + rightFileType.getExtension() + " pattern: " + rightPattern);
            int foundLeftCount = 0;

            Iterator<Material> leftIter = leftList.iterator();
            while (leftIter.hasNext()) {
                Material left = leftIter.next();
                final IFileType leftFileType = left.getIndexEntry().getFileType();
                final Metadata leftMetadata = left.getIndexEntry().getMetadata();
                if (leftFileType.equals(rightFileType) && (rightPattern.matches(leftMetadata)
                        || identifyMetadataValues.matches(leftMetadata))) {
                    MaterialProduct mp =
                            new MaterialProduct.Builder(left, right, resultTimestamp)
                                    .setQueryOnMetadata(rightPattern)
                                    .build();
                    mProductList.add(mp);
                    logger.debug(methodName + "left Y " + left.getShortID() + " "
                            + leftFileType.getExtension() + " " + leftMetadata);
                    foundLeftCount += 1;
                } else {
                    logger.debug(methodName + "left N " + left.getShortID() + " "
                            + leftFileType.getExtension() + " " + leftMetadata);
                }
            }
            if (foundLeftCount == 0) {
                MaterialProduct mp =
                        new MaterialProduct.Builder(Material.newEmptyMaterial(), right, resultTimestamp)
                                .setQueryOnMetadata(rightPattern)
                                .build();
                mProductList.add(mp);
            }

            logger.debug(methodName + "foundLeftCount=" + foundLeftCount);
            if (foundLeftCount == 0 || foundLeftCount >= 2) {
                logger.info(methodName + "foundLeftCount=" + foundLeftCount + " is unusual");
            }
        }

        //
        Iterator<Material> leftIter2 = leftList.iterator();
        while (leftIter2.hasNext()) {
            Material left = leftIter2.next();
            final IFileType leftFileType = left.getIndexEntry().getFileType();
            Metadata leftMetadata = left.getIndexEntry().getMetadata();
            final QueryOnMetadata leftPattern =
                    QueryOnMetadata.builder(leftMetadata, ignoreMetadataKeys).build();
            logger.debug(methodName + "--------");
            logger.debug(methodName + "left " + left.getShortID() + " "
                    + leftFileType.toString() + " pattern: " + leftPattern);
            int foundRightCount = 0;
            //
            Iterator<Material> rightIter2 = rightList.iterator();
            while (rightIter2.hasNext()) {
                Material right = rightIter2.next();
                final IFileType rightFileType = right.getIndexEntry().getFileType();
                final Metadata rightMetadata = right.getIndexEntry().getMetadata();
                if (rightFileType.equals(leftFileType) && (leftPattern.matches(rightMetadata)
                                || identifyMetadataValues.matches(rightMetadata))) {
                    // this must have been found matched already; no need to create a MProduct
                    logger.debug(methodName + "right Y " + right.getShortID() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                    foundRightCount += 1;
                } else {
                    logger.debug(methodName + "right N " + right.getShortID() + " " +
                            rightFileType.getExtension() + " " + rightMetadata);
                }
            }
            if (foundRightCount == 0) {
                MaterialProduct mProduct =
                        new MaterialProduct.Builder(left, Material.newEmptyMaterial(), resultTimestamp)
                        .setQueryOnMetadata(leftPattern)
                        .build();
                mProductList.add(mProduct);
            }
            logger.debug(methodName + "foundRightCount=" + foundRightCount);
            if (foundRightCount == 0 || foundRightCount >= 2) {
                logger.info(methodName + "foundRightCount=" + foundRightCount + " is unusual");
            }
        }
        Collections.sort(mProductList);
        return mProductList;
    }
}