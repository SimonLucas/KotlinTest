package tune

import ntbea.params.Param
import kotlin.reflect.jvm.internal.impl.descriptors.Named

data class NamedParam(val name: String, val values: Array<out Any>) {

}

//class ParamMapUtil(val params: Map<String, Array<out Any>>) {
//    fun report(solution: IntArray) : String {
//        val sb = StringBuilder()
//        for (i in 0 until solution.size) {
//            sb.append(String.format("%s\t %s\n", params[i].name, params[i].getValue(solution[i])))
//        }
//        return sb.toString()
//    }
//}

class ParamUtil(val params: Array<NamedParam>) {

    fun nValues() : ArrayList<Int> {
        val nValues = ArrayList<Int>()
        for (p in params) {
            nValues.add(p.values.size)
        }
        return nValues
    }

    fun report(solution: IntArray) : String {
        val sb = StringBuilder()
        for (i in 0 until solution.size) {
            sb.append(String.format("%s\t %s\n", params[i].name, params[i].values[ solution[i] ] ))
        }
        return sb.toString()
    }

}

