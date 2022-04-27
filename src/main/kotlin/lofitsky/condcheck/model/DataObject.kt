package lofitsky.condcheck.model

import lofitsky.condcheck.logic.dsl.ConditionDslElementDto


data class DataObject(
    val sources: Map<String, Any?>,
    val dsl: ConditionDslElementDto?,
)
