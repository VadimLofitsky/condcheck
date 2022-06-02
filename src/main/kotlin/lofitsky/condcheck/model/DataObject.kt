package lofitsky.condcheck.model

import lofitsky.condcheck.logic.dsl.ConditionDslElement


data class DataObject(
    val sources: Map<String, Any?>,
    val dsl: ConditionDslElement?,
)
