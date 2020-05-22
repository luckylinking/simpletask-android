package nl.mpcjanssen.simpletask

import nl.mpcjanssen.simpletask.task.Task

interface VisibleLine {
    val header: Boolean
    val task : Task?
    val title: String?
    val color: Int?
    val showCount: Boolean?
    val center: Boolean?
    val relTextSize: Float?
}

data class TaskLine(override val task: Task) : VisibleLine {
    override val title: String?
        get() = null
    override val header = false
    override val color: Int? = null
    override val showCount: Boolean? = null
    override val center: Boolean? = null
    override val relTextSize: Float? = null
}

data class HeaderLine(

    override var title: String,
    override val color: Int? = null,
    override val showCount: Boolean? = true,
    override val center: Boolean? = false,
    override val relTextSize: Float? = null

): VisibleLine{

    override val task: Task?
        get() =  null
    override val header = true

}