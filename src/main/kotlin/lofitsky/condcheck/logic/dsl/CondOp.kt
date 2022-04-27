package lofitsky.condcheck.logic.dsl


enum class CondOp(val op: (x1: Boolean, x2: Boolean?) -> Boolean, val sign: String? = null) {
    AND({ x1: Boolean, x2: Boolean? -> x1 && (x2 ?: true) }, " && "),
    OR({ x1: Boolean, x2: Boolean? -> x1 || (x2 ?: false) }, " || "),
    PLAIN({ x1: Boolean, _: Boolean? -> x1 });
}
