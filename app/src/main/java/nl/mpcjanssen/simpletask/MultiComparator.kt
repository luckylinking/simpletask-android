package nl.mpcjanssen.simpletask

import android.util.Log
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.util.alfaSort
import java.util.*
import nl.mpcjanssen.simpletask.MyInterpreter

class MultiComparator(sorts: ArrayList<String>, today: String, hourMinutesNow: String, caseSensitve: Boolean, createAsBackup: Boolean, moduleName: String? = null) {
    var comparator : Comparator<Task>? = null

    var fileOrder = true

    val luaCache: MutableMap<Task, String> = HashMap<Task, String>();

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
                    val txt = t.lists?.let { alfaSort(it).firstOrNull() } ?: ""
                    if (caseSensitve) {
                        txt
                    } else {
                        txt.toLowerCase(Locale.getDefault())
                    }
                }
                "by_project" -> comp = { t ->
                    val txt = t.tags?.let { alfaSort(it).firstOrNull() } ?: ""
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
                "completed" -> comp = { it.isCompleted() }
                "by_creation_date" -> comp = { it.createDate ?: lastDate }
//                "in_future" -> comp = { it.inFuture(today) }
                "by_due_date" -> comp = { it.dueDate ?: lastDate }
//                "by_threshold_date" -> comp = {
//                    val fallback = if (createAsBackup) it.createDate ?: lastDate else lastDate
//                    it.thresholdDate ?: fallback
//                }
// +modify
                "in_future" -> comp = { MyInterpreter.onThresholdSort(it, today) }
                "by_threshold_date" -> comp = { it.dueDate != null }
// -modify
                "by_completion_date" -> comp = { it.completionDate ?: lastDate }
                "by_lua" -> comp = { MyInterpreter.onSortCallback(it, today, hourMinutesNow)}

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
