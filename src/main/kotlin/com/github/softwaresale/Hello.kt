package com.github.softwaresale

import com.github.softwaresale.annotations.Column
import com.github.softwaresale.annotations.ColumnType
import com.github.softwaresale.annotations.RowModel
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@RowModel("student")
data class Student(
    @Column(name = "IUID", type = ColumnType.NUMERIC)
    var iuid: Long,
    @Column(name = "PUID", type = ColumnType.NUMERIC)
    var puid: Long,
    @Column(name = "First Name", type = ColumnType.STRING)
    var firstName: String,
    @Column(name = "Last Name", type = ColumnType.STRING)
    var lastName: String,
    @Column(name = "Campus Email", type = ColumnType.STRING)
    var email: String
)

data class ColumnInfo(
    val cellValueType: ColumnType,
    val cellIndex: Int,
    val fieldName: String
)

inline fun <reified T> readRowIntoModel(schemaRow: Row, modelRow: Row): T {
    // assert that type is a RowModel
    if (!T::class.java.isAnnotationPresent(RowModel::class.java)) {
        throw Exception("Type must be annotated with RowModel")
    }



    // For each Column in type, figure out what the cell index that type comes from based on schemaRow
    val columnInfo = mutableListOf<ColumnInfo>()

    println("Column info is $columnInfo")

    val paramMap = HashMap<KParameter, Any>()
    val constructor = T::class.constructors.toList()[0]
    columnInfo
        .map { info ->
            val cell = modelRow.getCell(info.cellIndex)
            val cellValue = when (info.cellValueType) {
                ColumnType.STRING -> cell.stringCellValue
                ColumnType.NUMERIC -> cell.numericCellValue
                ColumnType.BOOLEAN -> cell.booleanCellValue
            }

            val respectiveParameter = constructor.parameters.first { param -> param.name == info.fieldName }

            Pair<KParameter, Any>(respectiveParameter, cellValue)
        }
        .forEach { pair -> paramMap[pair.first] = pair.second }

    println("Param map is $paramMap")
    return constructor.callBy(paramMap)
}

fun main(args: Array<String>) {

    val workbook = XSSFWorkbook("C:\\Users\\chuck\\Documents\\Example Spreadsheet.xlsx")
    val worksheet = workbook.getSheet("Students")
    val schemaRow = worksheet.getRow(0)
    val secondRow = worksheet.getRow(1)

    println("member properties")
    Student::class.declaredMemberProperties.filter { prop -> prop.findAnnotation<Column>() != null }.forEach { println(it.name) }
    println()
    println("constructor params")
    Student::class.constructors.toList()[0].parameters.forEach { println(it.name) }
}

