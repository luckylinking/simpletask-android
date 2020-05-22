Changelog
=========

2020-05-14
-------
- 修正日程任务的判断逻辑：有开始或延迟时间的任务

2020-05-12
-------
- 提前一至两小时显示即将开始的任务（本小时显示下一小时的任务，不包括次日任务）

2020-05-11
-------
- 改“关键”分组为“限时”分组，增加“重要”分组
- 改“事务/日常”次分组为“事项/日程”次分组
- 调整了配色方案

2020-05-07
-------
- 重新调整了主分组结构，添加“事务/日常”次分组，梳理了分组层次和显示方式

2020-04-29
-------
- 将“完成”主分组分为“完成”和“完成日常”两个主分组
- 修改了主分组的字体大小和颜色
- "关键"分组不显示未到启动日期的内容

2020-04-28
-------
- 无优先级任务的标记改回“-”，排到有优先级任务之前。

2020-04-26
-------
调整了主分组方案
- 建议排序方式：   主分组、创建/完成日期（逆序）、中间分组、有无标签、标签、清单、截止日期、优先级、文件内部顺序（中间分组含义另见文件）

- 主分组：
        - 完成、COMPLETED   已经完成的任务（该分组顺序排在过目和将来之间，其余分组排序按本文顺序）
        - 便签、INBOX       新收集的想法或任务
            - 无启动日期、回顾日期、项目、清单的任务
        - 置顶、TOP	        置顶任务和提示
        - 关键、CRITICAL	今日必须完成的时效性任务，拖延可能造成你要加班或任务失败（建议不超过五条）
            - 今日或之前截止日期的任务
            - 今日或之前启动且有结束时间或设置了提醒的任务
        - 日常、DAILY	    已经启动的日常任务
            - 启动日期在今日之前的日常任务
            - 今日启动已到开始时间的非关键日常任务
        - 待办、TO-DO		今天计划要做的任务（其中高优先级者为重要任务，是当前价值最高、收益最大，应该投入主要精力的工作，建议不超过五条）
            - 除以上任务及今日启动但未到开始时间的任务之外，启动日期在今日或之前的任务
        - 过目、REVIEW		今天查看、考虑一下再根据情况做决定的任务（建议不超过二十条）
            - 今日启动但未到开始时间的任务
            - 回顾日期在今日或今日之前的任务
            - 无回顾日期任务中无启动日期的任务、启动日期在十天内的任务、截止日期在十天（十五天？）内的任务
        - 将来、FUTURE		日后再查看和处理的事务
            - 不属于以上的其他任务

2020-03-26
-------
分组参考日期可选

2020-03-06
-------
建议排序方式：主分组、创建/完成日期（逆序）、中间分组、有无标签、标签、优先级、截止日期 、字母顺序、文件内部顺序

修改主分组：
- 0 便签、INBOX - 新收集的想法或任务
    - 无启动日期、回顾日期、项目、清单的任务
- 1 置顶、TOP - 置顶任务和提示
- 2 当前日常、CURRENT - 已经启动的任务
    -  启动日期在今日之前的任务
    -  今日启动已到开始时间的非关键任务
- 3 今日关键、CRITICAL-TODAY - 今日必须完成的时效性任务，拖延可能造成你要加班或任务失败（建议不超过五条）
    - 今日或之前截止日期的任务
    - 今日或之前启动且有结束时间或设置了提醒的任务
- 4 今日待办、TODO-TODAY - 今天计划要做的任务（其中高优先级者为重要任务，是当前价值最高、收益最大，应该投入主要精力的工作，建议不超过五条）
    - 除3、2、5项之外启动日期在今日或之前的任务
- 6 今日日常、DAILY-TODAY - 今天的日常例行事务
    - 非优先级任务中启动日期在今日的每日、每工作日事务（未到开始时间）
- 5 今日过目、REVIEW-TODAY - 今天查看、考虑一下再根据情况做决定的任务（建议不超过二十条）
    - 回顾日期在今日或之前的任务
    - 无回顾日期任务中
        - 无启动日期的任务、启动日期在十天内的任务、截止日期在十天（十五天？）内的任务
            - 非无优先级的每日、每工作日（有启动日期）任务 
- 8 近期日常、NEAR-FUTURE-DAILY - 十日内的日常例行事务
    - 启动日期在十天内，非以上的每日、每工作日任务
- 7 日后事务、FUTURE-TASKS - 日后再查看和处理的事务
    - 不属于以上的其他任务
- 9 已完成、COMPLETED - 已经完成的任务

增加review date字段：r:YYYY-MM-DD

2019-07-19
-------
- 修改thresholdDate或deferDate时删除deferTime

2019-05-28
-------
- 增加若干字段：
    - 置底字段： bottom:xx              置底标记
    - 开始时间字段： begin:HH:mm        任务的开始时间（时：分）
    - 结束时间字段： end:HH:mm          任务的结束时间（时：分）
    - 推迟时间字段： defer:HH:mm         将任务推迟到当日某时间开始（时：分）
-   修改（添加）任务底部显示项：创建日期、到期日期、启动日期；推迟日期；开始时间、推迟时间、结束时间、重复方式
-   修改推迟菜单，可选择推迟到一周内的某天（显示星期几）

2018-07-27
-------
- 添加推迟日期显示
fun getRelativeDeferDate(task: Task, app: TodoApplication): String? {
    val date = task.deferDate ?: return null
    val S1 = getRelativeDate(app, "D: ", date).toString()
    val S2 = MyInterpreter.daysBetween(date, task.thresholdDate).toString()
    return "$S1 已推迟 $S2 天"
}

- 添加defer字段：d:xxxx-xx-xx，将启动日期延迟到某日（用于需要保留原始启动日期的情况，重复任务在生成新任务时自动删除）
- 添加top字段：top:XX，标记将任务置顶

Task.kt 添加内容：
data class DeferToken(override val valueStr : String) : KeyValueToken {
    override val key = "d"
}

data class TopToken(override val valueStr : String) : KeyValueToken {
    override val key = "top"
    override val value: Boolean
        get() = true
}

    var deferDate: String?
        get() = getFirstToken<DeferToken>()?.valueStr
        set(dateStr) {
            if (dateStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is DeferToken }
            } else {
                    upsertToken(DeferToken(dateStr!!))
            }
        }

    fun onTop(): Boolean {
        return getFirstToken<TopToken>()?.value == true
    }

插入内容：
//    fun markComplete(dateStr: String) : Task? {
//                val newTask = Task(textWithoutCompletedInfo)
                newTask.deferDate = null

                if (deferFromDate == "") deferFromDate = newTask.thresholdDate?: ""
//              if (newTask.thresholdDate != null) {
                    newTask.thresholdDate = null
//                  newTask.deferThresholdDate(pattern, deferFromDate)
                }
 
//
        private val MATCH_DEFER = Regex("[Dd]:(\\d{4}-\\d{2}-\\d{2})")
        private val MATCH_TOP = Regex("[Tt][Oo][Pp]:(.+)")
        
                MATCH_TOP.matchEntire(lexeme)?.let {
                    tokens.add(TopToken(it.groupValues[1]))
                    return@forEach
                }
                MATCH_DEFER.matchEntire(lexeme)?.let {
                    tokens.add(DeferToken(it.groupValues[1]))
                    return@forEach
                }
                
修改内容：
//    var thresholdDate: String?
//            } else {
                if (thresholdDate == null) {
                    upsertToken(ThresholdDateToken(dateStr!!))
                } else {
                    upsertToken(DeferToken(dateStr!!))
                }

- 日期显示中添加星期几（Util.kt 第631行添加     val weekString = MyInterpreter.dateToWeek(dateString)
                               第652行改为    val ss = SpannableString(prefix + s + " " + weekString)
                               ）
