package nl.mpcjanssen.simpletask.task

import nl.mpcjanssen.simpletask.util.addInterval
import java.util.*
import java.util.regex.Pattern

class Task(text: String, defaultPrependedDate: String? = null) {

    var tokens: List<TToken>

    init {
        tokens = parse(text)
        defaultPrependedDate?.let {
            if (createDate == null) {
                createDate = defaultPrependedDate
            }
        }
    }

    constructor (text: String) : this(text, null)

    fun update(rawText: String) {
        tokens = parse(rawText)
    }

    private inline fun <reified T> getFirstToken(): T? {
        tokens.filterIsInstance<T>().forEach {
            return it
        }
        return null
    }

    private inline fun <reified T : TToken> upsertToken(newToken: T) {

        if (getFirstToken<T>() == null) {
            tokens += newToken
        } else {
            tokens = tokens.map {
                if (it is T) {
                    newToken
                } else {
                    it
                }
            }
        }

    }

    val text: String
        get() {
            return tokens.joinToString(" ") { it.text }
        }

    val extensions: List<Pair<String, String>>
        get() {
            return tokens.asSequence().filter { it is KeyValueToken }.map {
                val token = it as KeyValueToken
                Pair(token.key, token.valueStr)
            }.toList()
        }

    var completionDate: String? = null
        get() = getFirstToken<CompletedDateToken>()?.value

    var uuid: String? = getFirstToken<UUIDToken>()?.valueStr

    var createDate: String?
        get() = getFirstToken<CreateDateToken>()?.value
        set(newDate) {
            val temp = ArrayList<TToken>()
            if (tokens.isNotEmpty() && (tokens.first() is CompletedToken)) {
                temp.add(tokens[0])
                tokens = tokens.drop(1)
                if (tokens.isNotEmpty() && tokens.first() is CompletedDateToken) {
                    temp.add(tokens.first())
                    tokens = tokens.drop(1)
                }
            }
            if (tokens.isNotEmpty() && tokens[0] is PriorityToken) {
                temp.add(tokens.first())
                tokens = tokens.drop(1)
            }
            if (tokens.isNotEmpty() && tokens[0] is CreateDateToken) {
                tokens = tokens.drop(1)
            }
            newDate?.let {
                temp.add(CreateDateToken(newDate))
            }
            temp.addAll(tokens)
            tokens = temp
        }

    var dueDate: String?
        get() = getFirstToken<DueDateToken>()?.valueStr
        set(dateStr) {
            if (dateStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is DueDateToken }
            } else {
                upsertToken(DueDateToken(dateStr))
            }
        }

    var thresholdDate: String?
        get() = getFirstToken<ThresholdDateToken>()?.valueStr
        set(dateStr) {
            if (dateStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is ThresholdDateToken }
            } else {
                if (thresholdDate == null) {
                    upsertToken(ThresholdDateToken(dateStr))
                } else {
                    upsertToken(DeferToken(dateStr))
                }
            }
        }

    var deferDate: String?
        get() = getFirstToken<DeferToken>()?.valueStr
        set(dateStr) {
            if (dateStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is DeferToken }
            } else {
                upsertToken(DeferToken(dateStr))
            }
        }

    var beginTime: String?
        get() = getFirstToken<BeginTimeToken>()?.valueStr
        set(timeStr) {
            if (timeStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is BeginTimeToken }
            } else {
                upsertToken(BeginTimeToken(timeStr))
            }
        }

    var endTime: String?
        get() = getFirstToken<EndTimeToken>()?.valueStr
        set(timeStr) {
            if (timeStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is BeginTimeToken }
            } else {
                upsertToken(BeginTimeToken(timeStr))
            }
        }

    var deferTime: String?
        get() = getFirstToken<DeferTimeToken>()?.valueStr
        set(timeStr) {
            if (timeStr.isNullOrEmpty()) {
                tokens = tokens.filter { it !is DeferTimeToken }
            } else {
                upsertToken(DeferTimeToken(timeStr))
            }
        }

    var priority: Priority
        get() = getFirstToken<PriorityToken>()?.value ?: Priority.NONE
        set(prio) {
            when {
                prio == Priority.NONE -> tokens = tokens.filter { it !is PriorityToken }
                tokens.any { it is PriorityToken } -> upsertToken(PriorityToken(prio.fileFormat))
                else -> tokens = listOf(PriorityToken(prio.fileFormat)) + tokens
            }
        }

    var recurrencePattern: String? = null
        get() = getFirstToken<RecurrenceToken>()?.valueStr

    var tags: SortedSet<String>? = null
        get() {
            tokens.filter { it is TagToken }.run {
                return if (size > 0) {
                    (map { it -> (it as TagToken).value }).toSortedSet()
                } else {
                    null
                }
            }
        }

    var lists: SortedSet<String>? = null
        get() {
            tokens.filter { it is ListToken }.run {
                return if (size > 0) {
                    (map { it -> (it as ListToken).value }).toSortedSet()
                } else {
                    null
                }
            }
        }

    var links: Set<String> = emptySet()
        get() {
            return tokens.asSequence().filter { it is LinkToken }.map { it -> (it as LinkToken).text }.toSet()
        }

    var phoneNumbers: Set<String> = emptySet()
        get() {
            return tokens.asSequence().filter { it is PhoneToken }.map { it -> (it as PhoneToken).text }.toSet()
        }
    var mailAddresses: Set<String> = emptySet()
        get() {
            return tokens.asSequence().filter { it is MailToken }.map { it -> (it as MailToken).text }.toSet()
        }
    var selected: Boolean = false
    val alphaParts: String = showParts { it.isAlpha() }

    fun removeTag(tag: String) {
        tokens = tokens.filter {
            !((it is TagToken) && it.value == tag)
        }
    }

    fun removeList(list: String) {
        tokens = tokens.filter {
            !((it is ListToken) && (it.value == list))
        }
    }

    fun markComplete(dateStr: String): Task? {
        if (!this.isCompleted()) {
            val textWithoutCompletedInfo = text
            tokens = listOf(CompletedToken(true), CompletedDateToken(dateStr)) + tokens
            val pattern = recurrencePattern
            if (pattern != null) {
                var deferFromDate = ""
                if (recurrencePattern?.contains("+") == false) {
                    deferFromDate = completionDate ?: ""
                }
                val newTask = Task(textWithoutCompletedInfo)

                newTask.deferDate = null    //  新任务不设置 推延日期
                newTask.deferTime = null    //  新任务不设置 推延时间

                if (newTask.dueDate != null) {
                    newTask.deferDueDate(pattern, deferFromDate)
                }

                if (deferFromDate == "") deferFromDate = newTask.thresholdDate?: "" //避免后面因为取消threshouldDate后丢失重复日期基准的问题
                if (newTask.thresholdDate != null) {
                    newTask.thresholdDate = null    //加上此操作，保证不会将日期设置为DeferDate
                    newTask.deferThresholdDate(pattern, deferFromDate)
                }
                if (!createDate.isNullOrEmpty()) {
                    newTask.createDate = dateStr
                }
                return newTask
            }
        }
        return null
    }

    fun markIncomplete() {
        tokens = tokens.filter {
            when (it) {
                is CompletedDateToken -> false
                is CompletedToken -> false
                else -> true
            }
        }
    }

    fun deferThresholdDate(deferString: String, deferFromDate: String) {
        if (MATCH_SINGLE_DATE.reset(deferString).matches()) {
            thresholdDate = deferString
            return
        }
        if (deferString == "") {
            thresholdDate = null
            return
        }
        val olddate: String? = if (deferFromDate.isEmpty()) {
            thresholdDate
        } else {
            deferFromDate
        }
        val newDate = addInterval(olddate, deferString)
        thresholdDate = newDate?.format(DATE_FORMAT)
    }

    fun deferDueDate(deferString: String, deferFromDate: String) {
        if (MATCH_SINGLE_DATE.reset(deferString).matches()) {
            dueDate = deferString
            return
        }
        if (deferString == "") {
            dueDate = null
            return
        }
        val olddate: String? = if (deferFromDate.isEmpty()) {
            dueDate
        } else {
            deferFromDate
        }
        val newDate = addInterval(olddate, deferString)
        dueDate = newDate?.format(DATE_FORMAT)
    }

    fun inFileFormat() = text

    fun inFuture(today: String): Boolean {
        val date = thresholdDate
        date?.let {
            return date > today
        }
        return false
    }

    fun isHidden(): Boolean {
        return getFirstToken<HiddenToken>()?.value ?: false
    }

    fun onTop(): Boolean {
        return getFirstToken<TopToken>()?.value ?: false
    }

    fun onBottom(): Boolean {
        return getFirstToken<BottomToken>()?.value ?: false
    }

    fun isCompleted(): Boolean {
        return getFirstToken<CompletedToken>() != null
    }

    fun showParts(filter: (TToken) -> Boolean): String {
        return tokens.asSequence().filter(filter)
                .joinToString(" ") { it.text }
    }

    fun getHeader(sort: String, empty: String, createIsThreshold: Boolean): String {
        when {
            sort.contains("by_context") -> {
                lists?.run {
                    if (size > 0) {
                        return first()
                    }
                }
                return empty

            }
            sort.contains("by_project") -> {
                tags?.run {
                    if (size > 0) {
                        return first()
                    }
                }
                return empty
            }
            sort.contains("by_threshold_date") -> return if (createIsThreshold) {
                thresholdDate ?: createDate ?: empty
            } else {
                thresholdDate ?: empty
            }
            sort.contains("by_prio") -> return priority.code
            sort.contains("by_due_date") -> return dueDate ?: empty
            else -> return ""
        }
    }

    /* Adds the task to list Listname
** If the task is already on that list, it does nothing
 */
    fun addList(listName: String) {
        listName.split(Regex("\\s+")).forEach {
            if (lists?.contains(it)!=true) {
                tokens += ListToken("@$it")
            }
        }
    }

    /* Tags the task with tag
    ** If the task already has te tag, it does nothing
    */
    fun addTag(tagName: String) {
        tagName.split(Regex("\\s+")).forEach {
            if (tags?.contains(it)!=true) {
                tokens += TagToken("+$it")
            }
        }
    }

    companion object {
        var TAG = "Task"
        const val DATE_FORMAT = "YYYY-MM-DD"
        private val MATCH_LIST = Pattern.compile("@(\\S+)").matcher("")
        private val MATCH_TAG = Pattern.compile("\\+(\\S+)").matcher("")
        private val MATCH_HIDDEN = Pattern.compile("[Hh]:([01])").matcher("")
        private val MATCH_UUID = Pattern
                .compile("[Uu][Uu][Ii][Dd]:([A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12})")
                .matcher("")
        private val MATCH_DUE = Pattern.compile("[Dd][Uu][Ee]:(\\d{4}-\\d{2}-\\d{2})").matcher("")
        private val MATCH_THRESHOLD = Pattern.compile("[Tt]:(\\d{4}-\\d{2}-\\d{2})").matcher("")
        private val MATCH_RECURRENCE = Pattern.compile("[Rr][Ee][Cc]:((\\+?)\\d+[dDwWmMyYbB])").matcher("")
        private val MATCH_EXT = Pattern.compile("(.+):(.+)").matcher("")
        private val MATCH_PRIORITY = Regex("\\(([A-Z])\\)")
        private val MATCH_SINGLE_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher("")
        private val MATCH_PHONE_NUMBER = Pattern.compile("[+]?[0-9,#()-]{4,}").matcher("")
        private val MATCH_URI = Pattern.compile("[a-z]+://(\\S+)").matcher("")
        private val MATCH_MAIL = Pattern.compile("[a-zA-Z0-9\\+\\._%\\-]{1,256}" + "@"
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+").matcher("")

        private val MATCH_DEFER = Pattern.compile("[Dd]:(\\d{4}-\\d{2}-\\d{2})").matcher("")
        private val MATCH_TOP = Pattern.compile("[Tt][Oo][Pp]:(.+)").matcher("")
        private val MATCH_BOTTOM = Pattern.compile("[Bb][Oo][Tt][Tt][Oo][Mm]:(.+)").matcher("")
        private val MATCH_BEGIN = Pattern.compile("[Bb][Ee][Gg][Ii][Nn]:(\\d{2}:\\d{2})").matcher("")
        private val MATCH_END = Pattern.compile("[Ee][Nn][Dd]:(\\d{2}:\\d{2})").matcher("")
        private val MATCH_DEFER_TIME = Pattern.compile("[Dd][Ee][Ff][Ee][Rr]:(\\d{2}:\\d{2})").matcher("")



        fun parse(text: String): ArrayList<TToken> {
            synchronized(this) {
                var lexemes = text.lex()
                val tokens = ArrayList<TToken>()

                if (lexemes.take(1) == listOf("x")) {
                    tokens.add(CompletedToken(true))
                    lexemes = lexemes.drop(1)
                    var nextToken = lexemes.getOrElse(0) { "" }
                    MATCH_SINGLE_DATE.reset(nextToken).apply {
                        if (matches()) {
                            tokens.add(CompletedDateToken(lexemes.first()))
                            lexemes = lexemes.drop(1)
                            nextToken = lexemes.getOrElse(0) { "" }
                            MATCH_SINGLE_DATE.reset(nextToken).apply {
                                if (matches()) {
                                    tokens.add(CreateDateToken(lexemes.first()))
                                    lexemes = lexemes.drop(1)
                                }
                            }
                        }
                    }
                }

                var nextToken = lexemes.getOrElse(0) { "" }
                MATCH_PRIORITY.matchEntire(nextToken)?.let {
                    tokens.add(PriorityToken(nextToken))
                    lexemes = lexemes.drop(1)
                }

                nextToken = lexemes.getOrElse(0) { "" }
                MATCH_SINGLE_DATE.reset(nextToken)?.apply {
                    if (matches()) {
                        tokens.add(CreateDateToken(lexemes.first()))
                        lexemes = lexemes.drop(1)
                    }
                }

                lexemes.forEach { lexeme ->
                    MATCH_LIST.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(ListToken(lexeme))
                            return@forEach
                        }
                    }
                    MATCH_TAG.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(TagToken(lexeme))
                            return@forEach
                        }
                    }
                    MATCH_HIDDEN.reset(lexeme)?.apply {
                        if (matches()) {
                            tokens.add(HiddenToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_TOP.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(TopToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_BOTTOM.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(BottomToken(group(1)))
                            return@forEach
                        }
                    }

                    // Match phone numbers before tags to support +31.....
                    // This will make tags which can also be interpreted as phone numbers not possible
                    MATCH_PHONE_NUMBER.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(PhoneToken(lexeme))
                            return@forEach
                        }
                    }

                    MATCH_DUE.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(DueDateToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_THRESHOLD.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(ThresholdDateToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_DEFER.reset(lexeme)?.apply {
                        if (matches()) {
                            tokens.add(DeferToken(group(1)))
                            return@forEach
                        }
                    }

                    MATCH_BEGIN.reset(lexeme)?.apply {
                        if (matches()) {
                            tokens.add(BeginTimeToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_END.reset(lexeme)?.apply {
                        if (matches()) {
                            tokens.add(EndTimeToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_DEFER_TIME.reset(lexeme)?.apply {
                        if (matches()) {
                            tokens.add(DeferTimeToken(group(1)))
                            return@forEach
                        }
                    }

                    MATCH_RECURRENCE.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(RecurrenceToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_UUID.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(UUIDToken(group(1)))
                            return@forEach
                        }
                    }
                    MATCH_URI.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(LinkToken(lexeme))
                            return@forEach
                        }
                    }
                    MATCH_MAIL.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(MailToken(lexeme))
                            return@forEach
                        }
                    }
                    MATCH_EXT.reset(lexeme).apply {
                        if (matches()) {
                            tokens.add(ExtToken(group(1), group(2)))
                            return@forEach
                        }
                    }

                    if (lexeme.isBlank()) {
                        tokens.add(WhiteSpaceToken(lexeme))
                    } else {
                        tokens.add(TextToken(lexeme))
                    }

                }

                return tokens
            }
        }
    }
}

interface TToken {
    val text: String
    val value: Any?
    fun isAlpha() = false
}

data class CompletedToken(override val value: Boolean) : TToken {
    override val text: String
        get() = if (value) "x" else ""

}

data class PriorityToken(override val text: String) : TToken {
    override val value: Priority
        get() = Priority.toPriority(text.removeSurrounding("(", ")"))

}

data class ListToken(override val text: String) : TToken {
    override val value: String
        get() = text.substring(1)

}

data class TagToken(override val text: String) : TToken {
    override val value: String
        get() = text.substring(1)

}

// Tokens with the same value as text representation
interface StringValueToken : TToken {
    override val value: String
        get () = text
}

data class CreateDateToken(override val text: String) : StringValueToken
data class CompletedDateToken(override val text: String) : StringValueToken
data class TextToken(override val text: String) : StringValueToken {
    override fun isAlpha(): Boolean {
        return true
    }
}

data class WhiteSpaceToken(override val text: String) : StringValueToken {
    override fun isAlpha(): Boolean {
        return true
    }
}

data class MailToken(override val text: String) : StringValueToken {
    override fun isAlpha(): Boolean {
        return true
    }
}

data class LinkToken(override val text: String) : StringValueToken {
    override fun isAlpha(): Boolean {
        return true
    }
}

data class PhoneToken(override val text: String) : StringValueToken {
    override fun isAlpha(): Boolean {
        return true
    }
}


// Key Value tokens
interface KeyValueToken : TToken {
    val key: String
    val valueStr: String
    override val value: Any?
        get() = valueStr
    override val text: String
        get() = "$key:$valueStr"
}

data class DueDateToken(override val valueStr: String) : KeyValueToken {
    override val key = "due"
}

data class ThresholdDateToken(override val valueStr: String) : KeyValueToken {
    override val key = "t"
}

data class DeferToken(override val valueStr : String) : KeyValueToken {
    override val key = "d"
}

data class RecurrenceToken(override val valueStr: String) : KeyValueToken {
    override val key = "rec"
}

data class HiddenToken(override val valueStr: String) : KeyValueToken {
    override val key = "h"
    override val value: Boolean
        get() = valueStr == "1"
}

data class ExtToken(override val key: String, override val valueStr: String) : KeyValueToken

data class UUIDToken(override val valueStr: String) : KeyValueToken {
    override val key = "uuid"
}

// Extension functions
fun String.lex(): List<String> = this.split(" ")

data class TopToken(override val valueStr : String) : KeyValueToken {
    override val key = "top"
    override val value: Boolean
        get() = true
}

data class BottomToken(override val valueStr : String) : KeyValueToken {
    override val key = "bottom"
    override val value: Boolean
        get() = true
}

data class BeginTimeToken(override val valueStr : String) : KeyValueToken {
    override val key = "begin"
}

data class EndTimeToken(override val valueStr : String) : KeyValueToken {
    override val key = "end"
}

data class DeferTimeToken(override val valueStr : String) : KeyValueToken {
    override val key = "defer"
}