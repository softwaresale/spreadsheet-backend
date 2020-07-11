package com.github.softwaresale.spreadsheetData.api.types

import com.github.softwaresale.spreadsheetData.annotations.Column
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

class SchemaRow<T : Any>(private val modelClass: KClass<T>) {

    private val columns = mutableListOf<Column>()

    init {
        // Load all of the columns into the
        modelClass.declaredMemberProperties
                .filter { it.findAnnotation<Column>() != null }
                .forEach { columns.add(it.findAnnotation()!!) } // will always be non-null because of the filter
    }
}