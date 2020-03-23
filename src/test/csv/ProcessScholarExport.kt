package test.csv

import com.opencsv.CSVReaderHeaderAware
import java.io.FileReader




fun main() {

    val filename = "../../../Downloads/pubs.csv"

    val values: Map<String, String> = CSVReaderHeaderAware(FileReader(filename)).readMap()

    for (key in values.keys) {
        println(key)
    }

    println(values.get("Pages"))

}