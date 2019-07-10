package nl.mpcjanssen.simpletask


import nl.mpcjanssen.simpletask.task.Task
import java.text.SimpleDateFormat
import java.util.*

object MyInterpreter {

    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    fun daysBetween(dateStrNew: String?, dateStrOld: String?): Int {
        val newLong = sdf.parse(dateStrNew?: "1970-01-01").time
        val oldLong = sdf.parse(dateStrOld?: "1970-01-01").time

        return ((newLong - oldLong)/24/3600/1000).toInt()
    }

//    fun secondsNow(): Int {
//        var calendar = Calendar.getInstance()
//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//        val minute = calendar.get(Calendar.MINUTE)
//        val second = calendar.get(Calendar.SECOND)
//
//        return hour * 3600 + minute * 60 + second
//    }

    fun hourMinuteNow(): String {
        val clock = SimpleDateFormat("HH:MM")
        return clock.format(System.currentTimeMillis())
    }

//    private val cal = Calendar.getInstance() // 获得一个日历

//    fun dateToWeek(dateTime: DateTime?): String? {
//        val weekDays=arrayOf("", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
//        return weekDays[dateTime?.weekDay ?: 0]
//
////        var datet = sdf.parse(dateTime)
////
////        cal.time=datet
////        var w=cal.get(Calendar.DAY_OF_WEEK) - 1 // 指示一个星期中的某天
////        if (w < 0) w = 0
//    }


    fun onSortCallback(f: Task, today: String, hourMinutesNow: String): String {

        val comp1 = onGroupSort1(f, today)
        val comp2 = onGroupSort2(f, today)
        val beginTime = f.deferTime ?: f.beginTime ?: hourMinutesNow

//        val seconds = seconds_input
//        val secondsString = java.lang.String.format( "%05d", seconds)

        return "$comp1$beginTime$comp2"

    }

    fun onGroupCallback(f: Task): List<String?> {
        val result = ArrayList<String?>(4)

        var prefix = ""
        var timeZone:String? = "　　"
        var tag:String? = "　　"
        var showCount:String? = ""

        val today = TodoApplication.app.today

        if (f.isCompleted()) prefix = "已完成" else {
            when (onGroupSort1(f, today)) {
                "1" -> prefix +="昨日"
                "2" -> {
                    prefix +="收集"
                    timeZone = null
                    tag = null
                }
                "3" -> {
                    prefix +="今日开始"
                    timeZone = null
                    tag = null
                    showCount = null
                }
                "4" -> prefix +="今日"
                "5" -> {
                    prefix +="今日终了"
                    timeZone = null
                    tag = null
                    showCount = null
                }
                "6" -> {
                    prefix +="备忘"
                    timeZone = null
                }
                "7" -> prefix +="明日"
                "8" -> prefix +="十天内"
                "9" -> prefix +="十天后"
            }
//            var listNow = f.lists
//            if(listNow != null) {
//                for (item in listNow) {
//                if (item.matches(Regex("［.*］"))) prefix="$prefix${item.removeSurrounding("［", "］")}"
//                }
//            }
//            prefix += "］"
        }
        result.add(prefix)

        if(timeZone!=null) timeZone += (f.deferTime ?: f.beginTime ?: "全时")
        if(timeZone!=null) timeZone += " @" + (f.lists ?: "未定场合").toString().removeSurrounding("[", "]").replace(",","、")

        result.add(timeZone)

        if(tag!=null) {
            when (onGroupSort2(f, today)) {
                "1" -> tag = "到期$tag"
                "2" -> tag = "限期$tag"
                else -> tag += "　　"
            }
            tag += f.tags?.sorted()?.firstOrNull() ?: "一般事务"
        }
        result.add(tag)
        result.add(showCount)

        return result

    }

    fun onThresholdSort(f: Task, today: String): Long {
        val inFuture = f.inFuture(today)

        var millionSeconds = sdf.parse(f.thresholdDate?:"1970-01-01").time
        if (inFuture) millionSeconds *= -1

        return millionSeconds
    }

    private fun onGroupSort1(f: Task, today: String): String {

//  按日期排序
//        "1" 昨日
//        "2" 收集
//        "3" 置顶
//        "4" 今日
//        "5" 置底
//        "6" 备忘
//        "7" 明日
//        "8" 十天内
//        "9" 十天后

        //当日置顶和置底
        if (f.onTop()) return "3"
        if (f.onBottom()) return "5"
//        if (f.text.matches(Regex("🌙✨.*"))) return "5"

        //无启动或截止时间
        val thresholdDateString = f.deferDate?: f.thresholdDate
        if ((thresholdDateString == null) && (f.dueDate == null)) {
            if (f.tags?.sorted()?.firstOrNull() == null) {
                return "2"  //无标签为收集任务
            } else {
                return "6"  //有标签为备忘任务
            }
        }

        //其余按启动时间分类，有截止时间但无启动时间的，默认启动时间为1970-1-1
        var thresholdLong = sdf.parse(thresholdDateString?:"1970-01-01").time

        val todayLong = sdf.parse(today).time
        val tomorrowLong = todayLong + 24*3600*1000
        val tenDaysLong = todayLong + 24*3600*1000 * 10

        if (thresholdLong < todayLong) return "1"
        if (thresholdLong < tomorrowLong) return "4"
        if (thresholdLong == tomorrowLong) return "7"
        if (thresholdLong < tenDaysLong) return "8"
        return "9"
    }

    private fun onGroupSort2(f: Task, today: String): String {
//  按截止时间排序
        val due = f.dueDate ?: return "9"
        if (due <= today) return "1"
        return  "2"
    }

}


