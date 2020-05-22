package nl.mpcjanssen.simpletask


import android.view.Gravity
import nl.mpcjanssen.simpletask.task.Task
import nl.mpcjanssen.simpletask.util.addInterval
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import androidx.core.content.ContextCompat
import nl.mpcjanssen.simpletask.task.Priority


enum class MainGroup {

    INBOX{
        override val title: String = "便签"
        override val showCompleteOrCreate: Boolean = true
        override val isScheduleGrouping: Boolean = false
    },

    TOP{
        override val title: String = "置顶"
        override val showTags: Boolean = true
        override val isScheduleGrouping: Boolean = false
    },

    CRITICAL{
        override val title: String = "限时"
        override val showThreshold: Boolean = true
        override val showSchedule: Boolean = true
        override val showTags: Boolean = true
        override val showLists: Boolean = true
        override val color: Int? = ContextCompat.getColor(TodoApplication.app, R.color.simple_red_dark)
    },

    IMPORTANT{
        override val title: String = "重要"
        override val showPriority: Boolean = true
        override val showSchedule: Boolean = true
        override val showTags: Boolean = true
        override val showLists: Boolean = true
        override val invertIsScheduleSort: Boolean = true
        override val color: Int? = ContextCompat.getColor(TodoApplication.app, R.color.simple_orange_dark)
    },

    TODO{
        override val title: String = "待办"
        override val showPriority: Boolean = true
        override val showSchedule: Boolean = true
        override val showTags: Boolean = true
        override val showLists: Boolean = true
        override val invertIsScheduleSort: Boolean = true

    },

    REVIEW{
        override val title: String = "查看"
        override val showThreshold: Boolean = true
        override val showSchedule: Boolean = true
        override val showTags: Boolean = true
        override val showLists: Boolean = true
    },

    COMPLETED{
        override val title: String = "完成"
        override val showCompleteOrCreate: Boolean = true
        override val showTags: Boolean = true
    },

    FUTURE{
        override val title: String = "将来"
        override val showThreshold: Boolean = true
        override val showReview: Boolean = true
        override val showSchedule: Boolean = true
        override val showTags: Boolean = true
        override val showLists: Boolean = true
    },

    ;

    open val title: String = ""
    open val showCompleteOrCreate: Boolean = false
    open val showThreshold: Boolean = false
    open val showPriority: Boolean = false
    open val showReview: Boolean = false
    open val showSchedule: Boolean = false
    open val showTags: Boolean = false
    open val showLists: Boolean = false
    open val isScheduleGrouping: Boolean = true
    open val invertIsScheduleSort: Boolean = false
    open val color: Int? = null
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

    fun relativeDate(dateStr: String?, beginTime: String? = null): String? {

        val relDate =
            if (dateStr == TodoApplication.app.today)
                if (beginTime ?: "00:00" < hourMinuteNow()) "今日" else "稍后"
            else
                dateStr

        return relDate
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


//建议排序方式：   主分组、创建/完成日期（逆序）、中间分组、有无标签、标签、清单、截止日期、优先级、文件内部顺序

//主分组：
//    未完成任务
//        便签、INBOX       新收集的想法或任务
//                              无启动日期、回顾日期、项目、清单的任务
//        置顶、TOP	        置顶任务和提示
//        关键、CRITICAL	今日必须完成的时效性任务，拖延可能造成你要加班或任务失败（建议不超过五条）
//                              今日或之前截止日期的任务
//                              今日或之前启动且有结束时间或设置了提醒的任务
//        日常、DAILY	    已经启动的日常任务
//                              启动日期在今日之前的日常任务
//                              今日启动已到开始时间的非关键日常任务
//        待办、TO-DO		今天计划要做的任务（其中高优先级者为重要任务，是当前价值最高、收益最大，应该投入主要精力的工作，建议不超过五条）
//                              除以上任务及今日启动但未到开始时间的任务之外，启动日期在今日或之前的任务
//        过目、REVIEW		今天查看、考虑一下再根据情况做决定的任务（建议不超过二十条）
//                              今日启动但未到开始时间的任务
//                              回顾日期在今日或之前的任务
//                              无回顾日期任务中无启动日期的任务、启动日期在十天内的任务、截止日期在十天（十五天？）内的任务
//        将来、FUTURE		日后再查看和处理的事务
//                              不属于以上的其他任务
//    已经完成的任务
//        完成、COMPLETED

    fun firstGrouping(f: Task, now: String = hourMinuteNow(), today: String = originDate?: TodoApplication.app.today): MainGroup {
        return group1(f)?: group2(f, now, today)
    }

    fun group1(f: Task): MainGroup? {

        if (f.isCompleted()) return MainGroup.COMPLETED
        if (f.onTop()) return MainGroup.TOP
        if (f.deferDate ?: f.thresholdDate == null &&f.reviewDate == null && f.lists == null && f.tags == null) return MainGroup.INBOX
        return null
    }

    fun group2(f: Task, now: String, today: String): MainGroup {

        val thresholdDate = f.deferDate ?: f.thresholdDate
        val dueDate = f.dueDate?:"9999-99-99"
        val endTime = f.endTime
        val priority = f.priority

        if (thresholdDate?.let{it < today} == true)
            return when {
                dueDate <= today || endTime != null     ->      MainGroup.CRITICAL
                priority == Priority.A                  ->      MainGroup.IMPORTANT
                else                                    ->      MainGroup.TODO
            }

        if (thresholdDate == today)
            return when {
                (f.deferTime?:f.beginTime)?.substring(0,2)?.toIntOrNull()?.let{ it > now.substring(0,2).toInt() + 1 }
                        == true                         ->      MainGroup.REVIEW
                dueDate <= today || endTime != null     ->      MainGroup.CRITICAL
                priority == Priority.A                  ->      MainGroup.IMPORTANT
                else                                    ->      MainGroup.TODO
            }

        val reviewDate = f.reviewDate
        if (reviewDate?.let{it <= today} == true) return MainGroup.REVIEW

        val relThreshold = daysBetween(thresholdDate, today)    //无启动日期者按1970-01-01
        val relDue = daysBetween(dueDate, today)
        return if (reviewDate == null && (relThreshold < 11 || relDue < 16)) MainGroup.REVIEW else MainGroup.FUTURE
//            if (f.tags?.sorted()?.firstOrNull() == null) {}
    }

    fun onSortCallback(f: Task): String {

        val comps = onGroupCallback(f)
        val comp3 = comps[3]?.sort?:"1970-01-01"
        val comp4 = comps[4]?.sort?:"_"
        val comp5 = comps[5]?.title?:"1970-01-01"
        val comp6 = comps[6]?.title?:"00:00"
        return "$comp3$comp4$comp5$comp6"

    }

    data class Group(
        val title: String?,
        val color: Int? = null,
        val relTextSize: Float? = null,
        val sort: String? = null,
        val mainGroup: MainGroup? = null,
        val showCount: Boolean? = null,
        val center: Boolean? = null
    )

    fun onGroupCallback(f: Task): ArrayList<Group?> {
        val result = ArrayList<Group?>(9)
        val nowTime = hourMinuteNow()
        val today = originDate?: TodoApplication.app.today
        val firstGroup = firstGrouping(f, nowTime, today)
        val startTime = f.deferTime ?: f.beginTime
        val isSchedule = f.isSchedule()
        val showThreshold = firstGroup.showThreshold || isSchedule && firstGroup.showSchedule
        val thresholdShow = if (showThreshold) f.deferDate?: f.thresholdDate else null

        result.add(                                                                     //index 0   主分组名称
            Group(firstGroup.title, firstGroup.color, 1.5f, mainGroup = firstGroup, showCount = false)
        )

        result.add(                                                                     //index 1   事项或日程分组
            (if (firstGroup.isScheduleGrouping) if (isSchedule) "日程" else "事项" else null)
                ?.let{Group(it, relTextSize = 1f)}
        )

        result.add(                                                                     //index 2   创建或完成日期
            (if (firstGroup.showCompleteOrCreate) f.completionDate?: f.createDate?: "1970-01-01"  else null)
                ?.let{Group(it, ContextCompat.getColor(TodoApplication.app, R.color.simple_green_dark))}
        )

        result.add(                                                                     //index 3   启动日期
            thresholdShow?.let{Group(relativeDate(it, startTime),
                ContextCompat.getColor(TodoApplication.app, R.color.simple_green_light),1f, it, center = true)}
        )

        result.add(                                                                     //index 4   优先级
            (if (!showThreshold && firstGroup.showPriority) f.priority.display+"优先级" else null)
                ?.let{Group(it,ContextCompat.getColor(TodoApplication.app, R.color.simple_green_light),
                    1f, f.priority.code, center = true)}
        )

        result.add(                                                                     //index 5   回顾日期
            (if (firstGroup.showReview) f.reviewDate else null)
                ?.let{Group(it,ContextCompat.getColor(TodoApplication.app, R.color.simple_green_dark))}
        )

        result.add(                                                                     //index 6   任务时间
            (if (firstGroup.showSchedule) startTime?:(if (thresholdShow != null) "--:--" else null) else null)
                ?.let{Group(it, ContextCompat.getColor(TodoApplication.app, R.color.simple_green_dark), center = true)}
        )

        val tags = (f.tags ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")
        val lists = (f.lists ?: "").toString().removeSurrounding("[", "]").replace(", ","｜")

        result.add(                                                                     //index 7   标签
            (
                if (firstGroup.showTags)
                    when (tags) {
                        "" -> "~"
                        else -> tags
                    }
                else
                    null
            )
                ?.let{Group(it, ContextCompat.getColor(TodoApplication.app, R.color.simple_blue_light))}
        )

        result.add(                                                                     //index 8   清单
            (
                if (firstGroup.showLists)
                    when (lists) {
                        ""  -> null
                        else -> "@$lists"
                    }
                else
                    null
            )
                ?.let{Group(it,ContextCompat.getColor(TodoApplication.app, R.color.gray67), center = true)}
        )

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


