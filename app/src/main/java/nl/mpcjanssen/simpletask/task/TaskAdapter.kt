package nl.mpcjanssen.simpletask.task

import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned.*
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.list_header.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import nl.mpcjanssen.simpletask.*
import nl.mpcjanssen.simpletask.util.*
import java.util.ArrayList

class TaskViewHolder(itemView: View, val viewType : Int) : RecyclerView.ViewHolder(itemView)

class TaskAdapter(val completeAction: (Task) -> Unit,
                  val unCompleteAction: (Task) -> Unit,
                  val onClickAction: (Task) -> Unit,
                  val onLongClickAction: (Task) -> Boolean) : RecyclerView.Adapter <TaskViewHolder>() {
    lateinit var query: Query
    val tag = "TaskAdapter"
    var textSize: Float = 14.0F
    override fun getItemCount(): Int {
        return visibleLines.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = when (viewType) {
            0 -> {
                // Header
                LayoutInflater.from(parent.context).inflate(R.layout.list_header, parent, false)
            }
            1 -> {
                // Task
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            }
            else -> {
                // Empty at end
                LayoutInflater.from(parent.context).inflate(R.layout.empty_list_item, parent, false)
            }

        }
        return TaskViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        when (holder.viewType) {
            0 -> bindHeader(holder, position)
            1 -> bindTask(holder, position)
            else -> return
        }
    }

    private fun bindHeader(holder : TaskViewHolder, position: Int) {
        val t = holder.itemView.list_header_title
        val line = visibleLines[position]
        val txt = line.title?:""
        val ss = SpannableString(txt)

        setColor(ss, line.color?: ContextCompat.getColor(TodoApplication.app, R.color.simple_orange_light))
        t.text = ss
        t.textSize = textSize * (line.relTextSize?: TodoApplication.config.headerRelativeSize)
        t.gravity = if (line.center==true) Gravity.CENTER else Gravity.START
    }

    private fun bindTask (holder : TaskViewHolder, position: Int) {
        val line = visibleLines[position]
        val task = line.task ?: return
        val view = holder.itemView
        val taskText = view.tasktext
        val taskAge = view.taskage
        val taskDue = view.taskdue
        val taskThreshold = view.taskthreshold
        val taskReview = view.taskreview
        val taskDefer = view.taskdefer
        val taskBegin = view.taskbegin
        val taskEnd = view.taskend
        val taskRec = view.taskrec
        val taskTimeDefer = view.timedefer

        val group = MyInterpreter.firstGrouping(task)
        val showBeginTime = !group.showSchedule
        val showLists = !group.showLists

        if (TodoApplication.config.showCompleteCheckbox) {
            view.checkBox.visibility = View.VISIBLE
        } else {
            view.checkBox.visibility = View.GONE
        }

        if (!TodoApplication.config.hasExtendedTaskView) {
            view.datebar1.visibility = View.GONE
            view.datebar2.visibility = View.GONE
            view.datebar3.visibility = View.GONE
        }
        val tokensToShowFilter: (it: TToken) -> Boolean = {
            when (it) {
                is UUIDToken -> false
                is CreateDateToken -> false
                is CompletedToken -> false
                is CompletedDateToken -> !TodoApplication.config.hasExtendedTaskView
                is DueDateToken -> !TodoApplication.config.hasExtendedTaskView
                is ThresholdDateToken -> !TodoApplication.config.hasExtendedTaskView
                is ListToken -> !query.hideLists || showLists
                is TagToken -> !query.hideTags
                is ReviewDateToken -> false
                is DeferToken -> false
                is TopToken -> false
                is BottomToken -> false
                is BeginTimeToken -> false
                is EndTimeToken -> false
                is RecurrenceToken -> false
                is DeferTimeToken -> false
                else -> true
            }
        }
        val txt = Interpreter.onDisplayCallback(query.luaModule, task) ?: task.showParts(tokensToShowFilter)
//        val txt2 = MyInterpreter.onSortCallback(task)
//        val txt2 = textSize.toString()
//        val txt ="$txt1$txt2"
        val ss = SpannableString(txt)
        if (TodoApplication.config.isDarkTheme || TodoApplication.config.isBlackTheme) {
            setColor(ss, Color.WHITE)
        } else {
            setColor(ss, Color.BLACK)
        }

        task.lists?.mapTo(ArrayList()) { "@$it" }?.let { setColor(ss, Color.GRAY, it) }
        task.tags?.mapTo(ArrayList()) { "+$it" }?.let { setColor(ss, Color.GRAY, it) }

        val priorityColor: Int
        val priority = task.priority
        priorityColor = when (priority) {
            Priority.A -> ContextCompat.getColor(TodoApplication.app, R.color.simple_orange_dark)
            Priority.B -> ContextCompat.getColor(TodoApplication.app, R.color.simple_orange_light)
            Priority.C -> ContextCompat.getColor(TodoApplication.app, R.color.simple_green_dark)
            Priority.D -> ContextCompat.getColor(TodoApplication.app, R.color.simple_green_light)
            Priority.E -> ContextCompat.getColor(TodoApplication.app, R.color.simple_blue_dark)
            Priority.F -> ContextCompat.getColor(TodoApplication.app, R.color.simple_blue_light)
            else -> ContextCompat.getColor(TodoApplication.app, R.color.gray67)
        }
        setColor(ss, priorityColor, priority.fileFormat)
        val completed = task.isCompleted()

        taskAge.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskDue.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskReview.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskThreshold.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskDefer.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskBegin.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskEnd.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskTimeDefer.textSize = textSize * TodoApplication.config.dateBarRelativeSize
        taskRec.textSize = textSize * TodoApplication.config.dateBarRelativeSize

        val cb = view.checkBox


        if (completed) {
            // Log.i( "Striking through " + task.getText());
            ss.setSpan(StrikethroughSpan(), 0 , ss.length, SPAN_INCLUSIVE_INCLUSIVE)
            taskAge.paintFlags = taskAge.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            cb.setOnClickListener { unCompleteAction(task) }
        } else {
            taskAge.paintFlags = taskAge.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            cb.setOnClickListener { completeAction(task) }

        }
        taskText.text = ss
        taskText.textSize = textSize
        handleEllipsis(taskText)
        cb.isChecked = completed

        val relAge = getRelativeAge(task, TodoApplication.app)
        val relDue = getRelativeDueDate(task, TodoApplication.app)
        val relReview = getRelativeReviewDate(task, TodoApplication.app)
        val relativeThresholdDate = getRelativeThresholdDate(task, TodoApplication.app)
        val relDefer = getRelativeDeferDate(task, TodoApplication.app)
        var txtBegin = task.beginTime
        if (txtBegin!=null) txtBegin = "开始: $txtBegin"
        var txtEnd = task.endTime
        if (txtEnd!=null) txtEnd = "结束: $txtEnd"
        var timeDefer = task.deferTime
        if (timeDefer!=null) timeDefer = "推迟: $timeDefer"
        var txtRec = task.recurrencePattern
        if (txtRec!=null) txtRec = "重复: " + txtRec.
                replace("d"," 日").
                replace("b", " 工作日").
                replace("w", " 星期").
                replace("m", " 月").
                replace("y", " 年").
                replace("+", "每 ").
                replace(" 1 ", "")


        if (!relAge.isNullOrEmpty() && (query.hideCreateDate || (relativeThresholdDate.isNullOrEmpty() && relReview.isNullOrEmpty()) ) ) {
            taskAge.text = relAge
            taskAge.visibility = View.VISIBLE
        } else {
            taskAge.text = ""
            taskAge.visibility = View.GONE
        }

        if (relDue != null) {
            taskDue.text = relDue
            taskDue.visibility = View.VISIBLE
        } else {
            taskDue.text = ""
            taskDue.visibility = View.GONE
        }

        if (!relReview.isNullOrEmpty()) {
            taskReview.text = relReview
            taskReview.visibility = View.VISIBLE
        } else {
            taskReview.text = ""
            taskReview.visibility = View.GONE
        }

        if (!relDefer.isNullOrEmpty()) {
            taskDefer.text = relDefer
            taskDefer.visibility = View.VISIBLE
        } else {
            taskDefer.text = ""
            taskDefer.visibility = View.GONE
        }

        if (!relativeThresholdDate.isNullOrEmpty()) {
            taskThreshold.text = relativeThresholdDate
            taskThreshold.visibility = View.VISIBLE
        } else {
            taskThreshold.text = ""
            taskThreshold.visibility = View.GONE
        }

        if (!txtBegin.isNullOrEmpty() && showBeginTime) {
            taskBegin.text = txtBegin
            taskBegin.visibility = View.VISIBLE
        } else {
            taskBegin.text = ""
            taskBegin.visibility = View.GONE
        }

        if (!txtEnd.isNullOrEmpty()) {
            taskEnd.text = txtEnd
            taskEnd.visibility = View.VISIBLE
        } else {
            taskEnd.text = ""
            taskEnd.visibility = View.GONE
        }

        if (!timeDefer.isNullOrEmpty()) {
            taskTimeDefer.text = timeDefer
            taskTimeDefer.visibility = View.VISIBLE
        } else {
            taskTimeDefer.text = ""
            taskTimeDefer.visibility = View.GONE
        }

        if (!txtRec.isNullOrEmpty()) {
            taskRec.text = txtRec
            taskRec.visibility = View.VISIBLE
        } else {
            taskRec.text = ""
            taskRec.visibility = View.GONE
        }

        // Set selected state
        // Log.d(tag, "Setting selected state ${TodoList.isSelected(item)}")
        view.isActivated = TodoApplication.todoList.isSelected(task)

        // Set click listeners
        view.setOnClickListener { onClickAction (task) ; it.isActivated = !it.isActivated }

        view.setOnLongClickListener { onLongClickAction (task) }
    }
    internal var visibleLines = ArrayList<VisibleLine>()

    internal fun setFilteredTasks(caller: Simpletask, newQuery: Query) {
        textSize = TodoApplication.config.tasklistTextSize ?: textSize
        Log.i(tag, "Text size = $textSize")
        query = newQuery

        caller.runOnUiThread {
            caller.showListViewProgress(true)
        }
        Log.i(tag, "setFilteredTasks called: ${TodoApplication.todoList}")
        val (visibleTasks, total) = TodoApplication.todoList.getSortedTasks(newQuery, TodoApplication.config.sortCaseSensitive)
        countTotalTasks = total
        countVisibleTasks = visibleTasks.size

        val newVisibleLines = ArrayList<VisibleLine>()

        newVisibleLines.addAll(addHeaderLines(visibleTasks, newQuery, getString(R.string.no_header)))

        caller.runOnUiThread {
            // Replace the array in the main thread to prevent OutOfIndex exceptions
            visibleLines = newVisibleLines
            caller.showListViewProgress(false)
            if (TodoApplication.config.lastScrollPosition != -1) {
                val manager = caller.listView?.layoutManager as LinearLayoutManager?
                val position = TodoApplication.config.lastScrollPosition
                val offset = TodoApplication.config.lastScrollOffset
                Log.i(tag, "Restoring scroll offset $position, $offset")
                manager?.scrollToPositionWithOffset(position, offset)
            }
            notifyDataSetChanged()
        }
    }


    var countVisibleTasks = 0
    var countTotalTasks = 0

    /*
    ** Get the adapter position for task
    */
    fun getPosition(task: Task): Int {
        val line = TaskLine(task = task)
        return visibleLines.indexOf(line)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == visibleLines.size) {
            return 2
        }
        val line = visibleLines[position]
        return if (line.header) {
            0
        } else {
            1
        }
    }

    private fun handleEllipsis(taskText: TextView) {
        val noEllipsizeValue = "no_ellipsize"
        val ellipsizeKey = TodoApplication.app.getString(R.string.task_text_ellipsizing_pref_key)
        val ellipsizePref = TodoApplication.config.prefs.getString(ellipsizeKey, noEllipsizeValue)

        if (noEllipsizeValue != ellipsizePref) {
            taskText.ellipsize = when (ellipsizePref) {
                "start" -> TextUtils.TruncateAt.START
                "end" -> TextUtils.TruncateAt.END
                "middle" -> TextUtils.TruncateAt.MIDDLE
                "marquee" -> TextUtils.TruncateAt.MARQUEE
                else -> {
                    Log.w(tag, "Unrecognized preference value for task text ellipsis: {} ! $ellipsizePref")
                    TextUtils.TruncateAt.MIDDLE
                }
            }
            taskText.maxLines = 1
            taskText.setHorizontallyScrolling(true)
        }
    }
}

