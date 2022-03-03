package com.kazurayam.materialstore.reduce.differ


import com.kazurayam.materialstore.reduce.MProduct
import com.kazurayam.materialstore.reduce.MProductGroup
import com.kazurayam.materialstore.reduce.Reducer
import com.kazurayam.materialstore.filesystem.FileType

interface DifferDriver extends Reducer {

    MProductGroup differentiate(MProductGroup mProductGroup)

    MProduct differentiate(MProduct mProduct)

    boolean hasDiffer(FileType fileType)
}