Changelog
=========
2019-05-28

增加若干字段：

置底字段： bottom:xx              置底标记
开始时间字段： begin:HH:MM        任务的开始时间（时：分）
结束时间字段： end:HH:MM          任务的结束时间（时：分）
推迟时间字段： defer:HH:MM         将任务推迟到当日某时间开始（时：分）

重新修改了排序方法

一些界面显示的修改：
使用三级标题（第一级：日期分段，第二集：开始或推迟时间，第三级：项目/标签）
修改（添加）任务底部显示项：创建日期、到期日期、启动日期；推迟日期；开始时间、推迟时间、结束时间、重复方式
修改推迟菜单，可选择推迟到一周内的某天（显示星期几）

2018-07-27

添加推迟日期显示
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

2018-07-20
-------
- SimpleTask改为 “事务”
- in_future 改为“启动日期（未启动任务按倒序）”
- by_lua 改为“我的自定义规则”
- by_threshold_date 改为“是否有截止日期” （避免多次按启动日期排序浪费时间）

- 新增MyInterpreter.kt
- 修改MultiComparator.kt中in_future
    "in_future" -> comp = { MyInterpreter.onThresholdSort(it, today) }
    "by_threshold_date" -> comp = { it.dueDate != null }
    "by_lua" -> comp = { MyInterpreter.onSortCallback(it, today, seconds)}
并新增seconds输入参数 seconds: Int,
- 修改TodoList中调用MultiComparator行，添加MyInterpreter.secondsNow(), 以匹配seconds输入参数
- 修改Util.kt，在181行替换null为MyInterpreter.onGroupCallback(t)

说明:
自定义排序/分组
    已完成：
        分组为“已完成”

    第一排序/分组：
        指定任务为特定分组5
        有启动日期但未启动的，
            明日启动的为明日任务7，
            十日内启动的为十日内任务8，
            十日外启动的为十日外任务9
        己启动或无启动日期的，
            有启动日期或有截止日期的，为当前任务（不注明）4，
            无启动日期且无截止日期的，有标签的为备忘任务6，否则为收集任务1
        有置顶标记的为置顶任务0
    第二排序/分组
        已到下一周期的周期任务列入到期任务1(仅对"+1d"起作用，"+1b"视同)
        今日已经到期的任务列入到期任务1
        其余截止日期的任务列入限期任务2
        无截止日期9
    第三排序/分组
        同类任务按当日时段排序/分组
    第四分组
        同时段同优先级按标签分组(无标签为一般事务)

排序推荐：
    1. 已完成（正序）
    2. 自定义（正序）
    3. 是否有截止日期（逆序）
    4. 优先级（正序）
    5. 标签（正序）
    6. 截止日期（正序）
    6. 启动日期（未启动任务按倒序）(逆序)
    7. 清单

