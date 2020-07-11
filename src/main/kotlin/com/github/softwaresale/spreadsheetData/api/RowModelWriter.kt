package com.github.softwaresale.spreadsheetData.api

import com.github.softwaresale.spreadsheetData.annotations.RowModel
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Writes a model to a table
 */
class RowModelWriter<T : Any>(
        private val modelClass: KClass<T>
) {
    init {
        if (modelClass.findAnnotation<RowModel>() == null) {
            throw IllegalStateException("modelClass must be annotated with com.github.softwaresale.spreadsheetData.annotations.RowModel")
        }
    }


}