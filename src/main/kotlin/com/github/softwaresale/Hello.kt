package com.github.softwaresale

import com.github.softwaresale.spreadsheetData.annotations.Column
import com.github.softwaresale.spreadsheetData.annotations.ColumnType
import com.github.softwaresale.spreadsheetData.annotations.RowModel
import com.github.softwaresale.spreadsheetData.api.RowModelParser
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@RowModel("student")
data class Student(
    @Column(name = "IUID", type = ColumnType.NUMERIC)
    var iuid: Double,
    @Column(name = "PUID", type = ColumnType.NUMERIC)
    var puid: Double,
    @Column(name = "First Name", type = ColumnType.STRING)
    var firstName: String,
    @Column(name = "Last Name", type = ColumnType.STRING)
    var lastName: String,
    @Column(name = "Campus Email", type = ColumnType.STRING)
    var email: String
)

fun main(args: Array<String>) {

    val workbook = XSSFWorkbook("C:\\Users\\chuck\\Documents\\Example Spreadsheet.xlsx")
    val worksheet = workbook.getSheet("Students")

    val parser = RowModelParser(Student::class, worksheet)
    val students = parser.parseRows()
    students.forEach { println(it) }
}

