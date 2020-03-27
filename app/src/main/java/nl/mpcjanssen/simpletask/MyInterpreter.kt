package nl.mpcjanssen.simpletask


import nl.mpcjanssen.simpletask.task.Priority
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.util.addInterval
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


enum class Groups {
    INBOX, TOP, DAILY_NOW, CRITICAL_TODAY, TODO_TODAY, DAILY_TODAY, REVIEW_TODAY, NEAR_FUTURE_DAILY, FUTURE_TASKS, COMPLETED;

    open val sort: Char =
        when(name) {
            "INBOX" -> '0'
            "TOP" -> '1'
            "DAILY_NOW" -> '2'
            "CRITICAL_TODAY" -> '3'
            "TODO_TODAY" -> '4'     //如修改此值需修改onSortCallback函数中comp4部分
            "REVIEW_TODAY" -> '5'   //如修改此值需修改onSortCallback函数中comp4部分
            "DAILY_TODAY" -> '6'
            "FUTURE_TASKS" -> '7'
            "NEAR_FUTURE_DAILY" -> '8'
            "COMPLETED" -> '9'
            else -> 'X'
        }

    open val title: String =
        when (name) {
            "INBOX" -> "便签"
            "TOP" -> "置顶"
            "DAILY_NOW" -> "当前日常"
            "CRITICAL_TODAY" -> "当日关键"
            "TODO_TODAY" -> "当日待办"
            "DAILY_TODAY" -> "当日日常"
            "REVIEW_TODAY" -> "当日过目"
            "NEAR_FUTURE_DAILY" -> "近期日常"
            "FUTURE_TASKS" -> "日后事务"
            "COMPLETED" -> "已完成"
            else -> "X"
        }

}


object MyInterpreter {

    private val MATCH_SINGLE_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher("")

    var originDate: String? = null
        set(dateStr) {
            field =
                when {
                    dateStr.isNullOrEmpty() -> null
                    MATCH_SINGLE_DATE.reset(dateStr).matches() -> dateStr
                    else -> addInterval(TodoApplication.app.today, dateStr)?.format(Task.DATE_FORMAT)
                }
        }

    fun daysBetween(dateStrNew: String?, dateStrOld: String?): Int {
        val day = SimpleDateFormat("yyyy-MM-dd")
        val newLong = day.parse(dateStrNew?: "1970-01-01").time
        val oldLong = day.parse(dateStrOld?: "1970-01-01").time
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

        val clock = SimpleDateFormat("HH:mm")
        return clock.format(Date())

//        return clock.format(System.currentTimeMillis())
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


//建议排序方式：主分组、创建/完成日期（逆序）、中间分组、有无标签、标签、优先级、截止日期 、字母顺序、文件内部顺序

//主分组：
//    未完成任务
//        0 便签、INBOX	                新收集的想法或任务
//                                          无启动日期、回顾日期、项目、清单的任务
//        1 置顶、TOP	                置顶任务和提示
//        2 当前日常、PRE-NOW	        已经启动的日常任务
//                                          启动日期在今日之前的日常任务
//                                          今日启动已到开始时间的非关键日常任务
//        3 今日关键、CRITICAL-TODAY		今日必须完成的时效性任务，拖延可能造成你要加班或任务失败（建议不超过五条）
//                                          今日或之前截止日期的任务
//                                          今日或之前启动且有结束时间或设置了提醒的任务
//        4 今日待办、TODO-TODAY		    今天计划要做的任务（其中高优先级者为重要任务，是当前价值最高、收益最大，应该投入主要精力的工作，建议不超过五条）
//                                          除3、2、5项之外启动日期在今日或之前的任务
//        5 今日日常、DAILY-TODAY	    今天的日常例行事务
//                                          非优先级任务中启动日期在今日的每日、每工作日事务（未到开始时间）
//        6 今日过目、REVIEW-TODAY		今天查看、考虑一下再根据情况做决定的任务（建议不超过二十条）
//                                          回顾日期在今日或之前的任务
//                                          无回顾日期任务中
//                                              无启动日期的任务、启动日期在十天内的任务、截止日期在十天（十五天？）内的任务
//                                                  非无优先级的每日、每工作日（有启动日期）任务
//        7 近期日常、NEAR-FUTURE-DAILY	    十日内的日常例行事务
//                                          启动日期在十天内，非以上的每日、每工作日任务
//        8 未来事务、FUTURE-TASKS		日后再查看和处理的事务
//                                          不属于以上的其他任务
//    已经完成的任务
//        9 已完成、COMPLETED

    fun firstGrouping(f: Task, now: String = hourMinuteNow(), today: String = originDate?: TodoApplication.app.today): Groups {
        return group1(f)?: group2(f, now, today)
    }

    fun group1(f: Task): Groups? {

        if (f.isCompleted()) return Groups.COMPLETED
        if (f.onTop()) return Groups.TOP
        if (f.deferDate ?: f.thresholdDate == null &&f.reviewDate == null && f.lists == null && f.tags == null) return Groups.INBOX
        return null
    }

    fun group2(f: Task, now: String, today: String): Groups {

        val thresholdDate = f.deferDate ?: f.thresholdDate
        val dueDate = f.dueDate?:"9999-99-99"
        val endTime = f.endTime
        val isDaily = f.isDaily() && (thresholdDate != null)

        if (dueDate <= today || (thresholdDate?.let{it <= today} == true && endTime != null)) return Groups.CRITICAL_TODAY
        if (isDaily  && ((thresholdDate?.let{it < today}) == true)) return Groups.DAILY_NOW
        if (thresholdDate == today && (f.beginTime?.let{ it < now }) == true)  return Groups.DAILY_NOW

        val noPriority = (f.priority == Priority.NONE)
        if (thresholdDate?.let{it <= today} == true) {
            return if (noPriority && isDaily) Groups.DAILY_TODAY else Groups.TODO_TODAY
        }

        val relThres = daysBetween(thresholdDate, today)
        val relDue = daysBetween(dueDate?:"9999-12-31", today)
        val reviewDate = f.reviewDate

        if (reviewDate?.let{it <= today} == true) return Groups.REVIEW_TODAY
        if (reviewDate == null && (relThres < 11 || relDue < 16)) {
            return if (isDaily && noPriority) Groups.NEAR_FUTURE_DAILY else Groups.REVIEW_TODAY
        }

        return Groups.FUTURE_TASKS
//            if (f.tags?.sorted()?.firstOrNull() == null) {}
    }

    fun onSortCallback(f: Task,taskGroup2By: String? = null): String {

        val comps = onGroupCallback(f, taskGroup2By)
        val comp1 = comps[0]
        val comp3 = comps[3]?:"1970-01-01"
        val comp4 = when (comp1) {
            "4","5" -> f.priority.code
            else -> "_"
        }
        val comp5 = comps[5]?.let {
            when {
                it.contains("当前") -> "#"
                it.contains("稍后") -> "-"
                else -> it
            }
        }?: "*"
        val comp6 = comps[6]?:"00:00"
        val comp7 = comps[7]?:""
        return "$comp1$comp3$comp4$comp5$comp6$comp7"
    }

    fun onGroupCallback(f: Task, taskGroup2By: String? = null): ArrayList<String?> {
        val result = ArrayList<String?>(9)
        val nowTime = hourMinuteNow()
        val today = originDate?: TodoApplication.app.today
        val thresholdDate = f.deferDate?: f.thresholdDate
        val firstGroup = firstGrouping(f, nowTime, today)
        result.add(firstGroup.sort.toString())      //index 0   主分组排序
        result.add(firstGroup.title)                //index 1   主分组名称

        val groupLabel2 = when (firstGroup) {
            Groups.INBOX -> f.createDate
            Groups.COMPLETED -> f.completionDate
            else -> null
        }
        result.add(groupLabel2)    //index 2    创建或完成时间

        val groupLabel3 = when (firstGroup) {
            Groups.INBOX,Groups.TOP,Groups.COMPLETED,Groups.DAILY_TODAY -> null
            else -> thresholdDate
        }
        if (groupLabel3 == today) result.add("当日") else result.add(groupLabel3)     //index 3   启动日期

        val groupLabel4 = when (firstGroup) {
            Groups.TODO_TODAY, Groups.REVIEW_TODAY -> f.priority.code
            else -> null
        }
        result.add(groupLabel4)     //index 4   优先级

        val beginTime = f.deferTime ?: f.beginTime
        val groupLabel5 = when (firstGroup) {
            Groups.DAILY_NOW,Groups.CRITICAL_TODAY,Groups.TODO_TODAY,Groups.DAILY_TODAY ->
                if (thresholdDate == TodoApplication.app.today)
                    when ( beginTime?.let{it < nowTime} ) {
                        true -> "当前"
                        null -> null
                        false -> "稍后（$nowTime）"
                    }
                else null
            Groups.FUTURE_TASKS -> f.reviewDate
            else -> null
        }
        result.add(groupLabel5)     //index 5 当前时间或回顾日期

        val groupLabel6 = when (firstGroup) {
            Groups.DAILY_NOW,Groups.CRITICAL_TODAY,Groups.TODO_TODAY,Groups.DAILY_TODAY,Groups.NEAR_FUTURE_DAILY -> beginTime ?: "--:--"
            else -> null
        }
        result.add(groupLabel6)     //index 6 任务时间

        val lists = (f.lists ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")
        val groupLabel7 = when (firstGroup) {
            Groups.DAILY_NOW,Groups.CRITICAL_TODAY,Groups.TODO_TODAY,Groups.DAILY_TODAY,Groups.NEAR_FUTURE_DAILY ->
                when (lists) {
                    ""  -> null
                    else -> "@$lists"
                }
            else -> null
        }
        result.add(groupLabel7)     //index 7   清单

        val tags = (f.tags ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")

        val groupLabel8 = when (firstGroup) {
            Groups.INBOX -> null
            else ->
                when (tags) {
                "" -> "~"
                else -> tags
            }

        }
        result.add(groupLabel8)     //index 8   标签

//        when {
//            secondGroupLabel == "" || secondGroupLabel == "@" -> result.add("普通事务")
//            secondGroupLabel?.lastOrNull() == '@' -> result.add(secondGroupLabel.substring(0, secondGroupLabel.length - 1))
//            else -> result.add(secondGroupLabel)
//        }

//            var listNow = f.lists
//            if(listNow != null) {
//                for (item in listNow) {
//                if (item.matches(Regex("［.*］"))) prefix="$prefix${item.removeSurrounding("［", "］")}"
//                }
//            }

//        result.add(prefix)
//
//        if(timeZone!=null) timeZone += (deferTime ?: f.beginTime ?: "全时")
//        if(timeZone!=null) timeZone += " @" + (f.lists ?: "未定场合").toString().removeSurrounding("[", "]").replace(",","、")
//
//        result.add(timeZone)

//        f.tags?.sorted()?.firstOrNull() ?: "任务")

        return result
    }

//    fun onThresholdSort(f: Task, today: String): Long {
//        val inFuture = f.inFuture(today)
//
//        var millionSeconds = sdf.parse(f.thresholdDate?:"1970-01-01").time
//        if (inFuture) millionSeconds *= -1
//
//        return millionSeconds
//    }

//    private fun onGroupSort3(f: Task, today: String): String {
////  按截止时间排序
//        val due = f.dueDate ?: return "9"  //无截止时间的排最后
//        if (due < today) return "1"  //截止时间在今日之前的排最前
//        if (due == today) return "2"  //截止时间在今日
//        return  "3" //有截止时间但未到
//    }

}


