package test.csv

import java.io.FileReader
import java.util.*
import com.opencsv.CSVReader


data class Pub(
        val authors: String = "",
        val title: String = "",
        val outlet: String = "",
        val volume: String = "",
        val number: String = "",
        val pages: String = "",
        val year: String = "",
        val publisher: String = ""

) {

    override fun toString(): String {
        return "$title, $authors, ($year), $outlet, $volume, pages: $pages, $publisher"
    }

    fun intYear(): Int {
        return (
                try {
                    year.toInt()
                } catch (
                        e: Exception
                ) {
                    0
                }
                )
    }
}

enum class Cols {
    Authors, Title, Outlet, Volume, Number, Pages, Year, Publisher
}


fun main(args: Array<String>) { //Build reader instance

    val filename = "../../../Downloads/pubs.csv"

    val reader = CSVReader(FileReader(filename))
    // reader.
    val allRows: List<Array<String?>> = reader.readAll()

    val pubs = ArrayList<Pub>()

    //Read CSV line by line and use the string array as you want
    for (row in allRows) {
        val pub = Pub(
                title = row[Cols.Title.ordinal]!!,
                authors = row[Cols.Authors.ordinal]!!,
                outlet = row[Cols.Outlet.ordinal]!!,
                publisher = row[Cols.Publisher.ordinal]!!,
                volume = row[Cols.Volume.ordinal]!!,
                number = row[Cols.Number.ordinal]!!,
                pages = row[Cols.Pages.ordinal]!!,
                year = row[Cols.Year.ordinal]!!
        )
        if (pub.intYear() >= 2015) {
            pubs.add(pub)
        }
    }

    val sorted = pubs.sortedByDescending { it.year }

    for (pub in sorted)
        println(pub)

    println("Filtered rows: ${sorted.size} ")
}