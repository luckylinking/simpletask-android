package nl.mpcjanssen.simpletask

import nl.mpcjanssen.simpletask.task.Task

interface VisibleLine {
    val header: Boolean
    val task : Task?
    val title: String?
    val level: Int?
}

data class TaskLine(override val task: Task) : VisibleLine {
    override val title: String?
        get() = null
    override val level: Int?
        get() = null
    override val header = false

}

data class HeaderLine(override var title: String, override var level: Int) : VisibleLine {
    override val task: Task?
        get() =  null
    override val header = true
}