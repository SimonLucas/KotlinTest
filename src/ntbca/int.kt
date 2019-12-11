package ntbca

import utilities.StatSummary

interface CoevModel {
    fun addPoint(p: IntArray, q: IntArray, pv: Double, qv: Double) : CoevModel
    fun getStats(p: IntArray, q: IntArray) : Pair<StatSummary,StatSummary>
}

