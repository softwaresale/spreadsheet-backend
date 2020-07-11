package com.github.softwaresale.spreadsheetData.annotations

import kotlin.reflect.KClass

enum class ColumnType(val ktype: KClass<*>) {
    STRING(String::class),
    NUMERIC(Double::class),
    BOOLEAN(Boolean::class)
}

@Target(AnnotationTarget.PROPERTY)
annotation class Column(val name: String, val type: ColumnType)