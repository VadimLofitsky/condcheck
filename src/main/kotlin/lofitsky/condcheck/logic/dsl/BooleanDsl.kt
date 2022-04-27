package lofitsky.condcheck.logic.dsl

import lofitsky.condcheck.logic.dsl.CondOp.*
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.standard.SpelExpressionParser


class Builder {
    private val parser = SpelExpressionParser()
    private var rootDslElement: ConditionDslElement? = null

    fun appendDsl(dsl: () -> ConditionDslElement): Builder {
        rootDslElement = dsl()
        return this
    }

    fun collect(context: EvaluationContext): ConditionDslElementDto?
        = rootDslElement?.eval(context, parser)?.let { ConditionDslElementDto(it) }
}

interface Element {
    fun eval(evContext: EvaluationContext, parser: SpelExpressionParser): ConditionDslElement
}

abstract class ConditionDslElement(
    val type: CondOp,
    val name: String? = null,
    var title: String? = null,
    var cond: String? = null,
) : Element {
    var subConds: MutableList<ConditionDslElement>? = null
    var state: Boolean? = null
        private set
    var childrenCount: Int = 0
        private set
    var stringified: String =""
        private set

    protected fun <T : ConditionDslElement> initElement(tag: T, init: (T.() -> Unit)?): T {
        init?.also { it.invoke(tag) }
        subConds + tag
        return tag
    }

    override fun eval(evContext: EvaluationContext, parser: SpelExpressionParser): ConditionDslElement {
        if(type == PLAIN && cond != null) {
            state = parser.parseExpression(cond!!).getValue(evContext, Boolean::class.java) ?: false
            childrenCount = 0
            stringified = cond ?: ""
            return this
        }

        state = subConds
            ?.mapNotNull { it.eval(evContext, parser)?.state }
            ?.reduce { acc, b -> type.op(acc, b) }
            ?: false

        childrenCount = subConds?.sumOf { if(it.type == PLAIN) 1 else it.childrenCount } ?: 0

        stringified = subConds
            ?.filter { it.stringified.isNotBlank() }
            ?.joinToString(type.sign ?: "") { it.stringified }
            ?.let { "($it)" }
            ?: ""

        return this
    }

    operator fun MutableList<ConditionDslElement>?.plus(condElement: ConditionDslElement): Unit {
        if(subConds == null) subConds = mutableListOf()
        subConds!!.add(condElement)
    }

    operator fun plus(condElement: ConditionDslElement): Unit {
        subConds?.add(condElement) ?: run { subConds = mutableListOf<ConditionDslElement>().also { it.add(condElement) } }
    }
}

data class ConditionDslElementDto(
    val type: String,
    val title: String?,
    val state: Boolean,
    val cond: String?,
    val subConds: List<ConditionDslElementDto>?,
    var childrenCount: Int = 0,
    val stringified: String,
) {
    constructor(dsl: ConditionDslElement): this(
        type = dsl.type.name,
        title = dsl.title,
        state = dsl.state!!,
        cond = dsl.cond,
        subConds = dsl.subConds?.map { ConditionDslElementDto(it) },
        childrenCount = dsl.childrenCount,
        stringified = dsl.stringified,
    )
}

class PlainCond(cond: String, title: String? = null) : ConditionDslElement(PLAIN, name = "t", cond = cond, title = title)

abstract class ConditionDslBoolElement(condOp: CondOp, name: String, title: String? = null) : ConditionDslElement(condOp, name = name, title = title) {
    fun and(title: String? = null, init: AndOp.() -> Unit) = initElement(AndOp(title), init)
    fun or(title: String? = null, init: OrOp.() -> Unit) = initElement(OrOp(title), init)
    fun p(title: String?, cond: String, init: (PlainCond.() -> Unit)? = null) = initElement(PlainCond(cond, title), init)
}

class AndOp(title: String? = null) : ConditionDslBoolElement(AND, name = "and", title = title)
class OrOp(title: String? = null) : ConditionDslBoolElement(OR, name = "or", title = title)

fun and(title: String? = null, init: AndOp.() -> Unit): AndOp = AndOp(title).also(init)
fun or(title: String? = null, init: OrOp.() -> Unit): OrOp = OrOp(title).also(init)
fun p(title: String?, cond: String, init: (PlainCond.() -> Unit)? = null): PlainCond = PlainCond(cond, title).also { init?.also { l -> l.invoke(it) } }
