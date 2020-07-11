package com.github.softwaresale.spreadsheetData.api

import com.github.softwaresale.spreadsheetData.annotations.Column
import com.github.softwaresale.spreadsheetData.annotations.ColumnType
import com.github.softwaresale.spreadsheetData.annotations.RowModel
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Parses a row or a number of rows into a class
 */
class RowModelParser<T : Any>(
        private val parsedTypeClass: KClass<T>,
        private val worksheet: XSSFSheet,
        private val schemaStartXY: Pair<Int, Int> = Pair(0, 0)
) {

    init {
        // Assert that parsedTypeClass has annotation
        if (parsedTypeClass.findAnnotation<RowModel>() == null) {
            throw IllegalStateException("parsedTypeClass must be annotated with com.github.softwaresale.spreadsheetData.annotations.RowModel")
        }
    }

    private val schemaRow: Row = worksheet.getRow(schemaStartXY.second)
    private var schemaRowIterator: Iterator<IndexedValue<Cell>> = schemaRow.cellIterator().withIndex()
    private val columns = parsedTypeClass.declaredMemberProperties
            .filter { it.findAnnotation<Column>() != null }
            .map { Pair(it, it.findAnnotation<Column>()) }

    init {
        // Make the schemaRowIterator start at the first cell of the schema
        this.popIteratorTo(this.schemaRowIterator, this.schemaStartXY.first)
    }

    /**
     * Parses a row into a model
     */
    fun parseRow(modelRow: Row): T {
        // Start the cell iterator at the right position
        val modelRowCellIterator = modelRow.cellIterator()
        this.popIteratorTo(modelRowCellIterator, schemaStartXY.first)

        // Get constructor
        val modelConstructor = parsedTypeClass.constructors.toList()[0]
        val modelConstructorParameters = modelConstructor.parameters

        val paramMap = HashMap<KParameter, Any>()
        // Map columns into schema row index
        schemaRowIterator.forEachRemaining { schemaCell ->
            // Find the column that corresponds to the schema
            val column = columns.find { it.second!!.name == schemaCell.value.stringCellValue }

            val valueCell = modelRow.getCell(schemaCell.index)
            val valueCellValue: Any = with(column?.second?.type) {
                when(this) {
                    ColumnType.STRING -> valueCell.stringCellValue
                    ColumnType.NUMERIC -> valueCell.numericCellValue
                    ColumnType.BOOLEAN -> valueCell.booleanCellValue
                    else -> throw IllegalStateException("Could not read value from cell")
                }
            }

            // Get the parameter that corresponds to the property
            val thisParam = modelConstructorParameters.find { it.name == column?.first?.name }

            paramMap[thisParam!!] = valueCellValue
        }

        // Return a constructed instance
        return modelConstructor.callBy(paramMap)
    }

    /**
     * Parse a collection of rows between two Y values
     */
    fun parseRows(startY: Int = this.schemaStartXY.second + 1, endY: Int = startY): List<T> {
        if (startY < schemaStartXY.second || endY < schemaStartXY.second) {
            throw IllegalStateException("Neither startY or endY can be above schema")
        }

        if (endY < startY) {
            throw IllegalStateException("endY cannot come before startY")
        }

        // Get a model row
        val rows = (startY..endY).map { y -> worksheet.getRow(y) }

        return parseRows(rows)
    }

    /**
     * Parses an actual row iterable
     */
    private fun parseRows(rows: List<Row>): List<T> {
        return rows.map { parseRow(it) }
    }

    private fun popIteratorTo(iterator: Iterator<*>, idx: Int) {
        for (i in 0 until idx) iterator.next()
    }

    private fun resetIterator() {
        this.schemaRowIterator = worksheet.getRow(schemaStartXY.second).cellIterator().withIndex()
    }
}