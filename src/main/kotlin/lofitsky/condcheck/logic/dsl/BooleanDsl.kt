package lofitsky.condcheck.logic.dsl

import lofitsky.condcheck.logic.dsl.CondOp.*
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.standard.SpelExpressionParser


class Builder {
    private val parser = SpelExpressionParser()
    private var rootDslElement: AbstarctConditionDslElement? = null

    fun appendDsl(dsl: () -> AbstarctConditionDslElement): Builder {
        rootDslElement = dsl()
        return this
    }

    fun collect(context: EvaluationContext): ConditionDslElement?
        = rootDslElement?.eval(context, parser)?.let { ConditionDslElement(it) }
}

interface Element {
    fun eval(evContext: EvaluationContext, parser: SpelExpressionParser): AbstarctConditionDslElement
}

abstract class AbstarctConditionDslElement(
    val type: CondOp,
    val name: String? = null,
    var title: String? = null,
    var cond: String? = null,
) : Element {
    var subConds: MutableList<AbstarctConditionDslElement>? = null
    var state: Boolean? = null
        private set
    var childrenCount: Int = 0
        private set
    var stringified: String =""
        private set

    protected fun <T : AbstarctConditionDslElement> initElement(tag: T, init: (T.() -> Unit)?): T {
        init?.also { it.invoke(tag) }
        subConds + tag
        return tag
    }

    override fun eval(evContext: EvaluationContext, parser: SpelExpressionParser): AbstarctConditionDslElement {
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

    operator fun MutableList<AbstarctConditionDslElement>?.plus(condElement: AbstarctConditionDslElement): Unit {
        if(subConds == null) subConds = mutableListOf()
        subConds!!.add(condElement)
    }

    operator fun plus(condElement: AbstarctConditionDslElement): Unit {
        subConds?.add(condElement) ?: run { subConds = mutableListOf<AbstarctConditionDslElement>().also { it.add(condElement) } }
    }
}

data class ConditionDslElement(
    val type: String,
    val title: String?,
    val state: Boolean,
    val cond: String?,
    val subConds: List<ConditionDslElement>?,
    var childrenCount: Int = 0,
    val stringified: String,
) {
    constructor(dsl: AbstarctConditionDslElement): this(
        type = dsl.type.name,
        title = dsl.title,
        state = dsl.state!!,
        cond = dsl.cond,
        subConds = dsl.subConds?.map { ConditionDslElement(it) },
        childrenCount = dsl.childrenCount,
        stringified = dsl.stringified,
    )
}

class PlainCond(cond: String, title: String? = null) : AbstarctConditionDslElement(PLAIN, name = "t", cond = cond, title = title)

abstract class AbstarctConditionDslBoolElement(condOp: CondOp, name: String, title: String? = null) : AbstarctConditionDslElement(condOp, name = name, title = title) {
    fun and(title: String? = null, init: AndOp.() -> Unit) = initElement(AndOp(title), init)
    fun or(title: String? = null, init: OrOp.() -> Unit) = initElement(OrOp(title), init)
    fun p(title: String?, cond: String, init: (PlainCond.() -> Unit)? = null) = initElement(PlainCond(cond, title), init)
}

class AndOp(title: String? = null) : AbstarctConditionDslBoolElement(AND, name = "and", title = title)
class OrOp(title: String? = null) : AbstarctConditionDslBoolElement(OR, name = "or", title = title)

fun and(title: String? = null, init: AndOp.() -> Unit): AndOp = AndOp(title).also(init)
fun or(title: String? = null, init: OrOp.() -> Unit): OrOp = OrOp(title).also(init)
fun p(title: String?, cond: String, init: (PlainCond.() -> Unit)? = null): PlainCond = PlainCond(cond, title).also { init?.also { l -> l.invoke(it) } }
