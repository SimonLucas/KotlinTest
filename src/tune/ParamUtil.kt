package tune

import ntbea.params.Param

class ParamUtil(val params: Array<Param>) {

    fun nValues() : ArrayList<Int> {
        val nValues = ArrayList<Int>()
        for (p in params) {
            // nValues.add(p.)
        }
        return nValues
    }

    fun report(solution: IntArray) : String {
        val sb = StringBuilder()
        for (i in 0 until solution.size) {
            sb.append(String.format("%s\t %s\n", params[i].name, params[i].getValue(solution[i])))
        }
        return sb.toString()
    }

}

