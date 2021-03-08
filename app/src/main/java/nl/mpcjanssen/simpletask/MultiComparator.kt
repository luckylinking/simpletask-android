package nl.mpcjanssen.simpletask

import android.util.Log
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.util.alfaSort
import java.util.*
import nl.mpcjanssen.simpletask.MyInterpreter
import kotlin.math.abs

class MultiComparator(sorts: ArrayList<String>, today: String, caseSensitve: Boolean, createIsThreshold: Boolean, moduleName: String? = null) {
    var comparator : Comparator<Task>? = null

    var fileOrder = true

//    val luaCache: MutableMap<Task, String> = HashMap<Task, String>();

    init {
        label@ for (sort in sorts) {
            val parts = sort.split(Query.SORT_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var reverse = false
            val sortType: String
            if (parts.size == 1) {
                // support older shortcuts and widgets
                reverse = false
                sortType = parts[0]
            } else {
                sortType = parts[1]
                if (parts[0] == Query.REVERSED_SORT) {
                    reverse = true
                }
            }
            var comp: (Task) -> Comparable<*>
            val lastDate = "9999-99-99"
            when (sortType) {
                "file_order" -> {
                    fileOrder = !reverse
                    break@label
                }
                "by_context" -> comp = { t ->
//                    val txt = t.lists?.let { alfaSort(it).firstOrNull() } ?: ""
                    val txt = (t.lists ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")
                    if (caseSensitve) {
                        txt
                    } else {
                        txt.toLowerCase(Locale.getDefault())
                    }
                }
                "by_project" -> comp = { t ->
//                    val txt = t.tags?.let { alfaSort(it).firstOrNull() } ?: ""
                    val txt = (t.tags ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")
                    if (caseSensitve) {
                        txt
                    } else {
                        txt.toLowerCase(Locale.getDefault())
                    }
                }
                "alphabetical" -> comp = if (caseSensitve) {
                    { it.alphaParts }
                } else {
                    { it.alphaParts.toLowerCase(Locale.getDefault()) }
                }
                "by_prio" -> comp = { it.priority }
//                "completed" -> comp = { it.isCompleted() }
                "completed" -> comp = {                 //改为按创建或完成时间排序
                    if (MyInterpreter.group1(it)?.mainGroup?.showCompleteOrCreate == true) it.completionDate ?: it.createDate ?: "1970-01-01" else "1970-01-01"
                }
                "by_creation_date" -> comp = { it.createDate ?: lastDate }
//                "in_future" -> comp = { it.inFuture(today, createIsThreshold) }
                "in_future" -> comp = { when (it.tags.isNullOrEmpty()) {    //     改为按有无标签排序
                    true -> "0"
                    false -> "1"
                } }
                "by_due_date" -> comp = { it.dueDate ?: lastDate }
//                "by_threshold_date" -> comp = {
//                    val fallback = if (createIsThreshold) it.createDate ?: lastDate else lastDate
//                    it.thresholdDate ?: fallback
//                }
                "by_threshold_date" -> comp = { MyInterpreter.onSortCallback(it) }    //改为按中间分组排序
                "by_completion_date" -> comp = {                                        //改为按距基准日期间隔排序
                    val baseDate = MyInterpreter.originDate ?: today
                    val thresholdDate = it.thresholdDate ?: it.createDate
                    if (thresholdDate == null) 0 else abs(MyInterpreter.daysBetween(thresholdDate, baseDate))
//                    it.completionDate ?: lastDate
                }
                "by_lua" -> comp = {                                                                //改为按主分组排序
                    val group = MyInterpreter.firstGrouping(it)
                    if (group.inLater) {
                        group.mainGroup.inFuture + 1
                    } else {
                        group.mainGroup.inFuture
                    }.toString() +
                            group.mainGroup.ordinal.toString() +
                            if (it.isSchedule()) "1" else if (group.mainGroup.invertIsScheduleSort) "2" else "0"
                }

                else -> {
                    Log.w("MultiComparator", "Unknown sort: $sort")
                    continue@label
                }
            }
            comparator = if (reverse) {
                comparator?.thenByDescending(comp) ?: compareByDescending(comp)
            } else {
                comparator?.thenBy(comp) ?: compareBy(comp)
            }
        }
    }

}

