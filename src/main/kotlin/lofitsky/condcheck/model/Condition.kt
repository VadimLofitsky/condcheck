package lofitsky.condcheck.model


class Condition(
    val title: String?,
    val cond: String,
    val subConds: List<Condition> = emptyList(),
) {
//    private val condOpFunx: Map<CondOp, (List<Condition>) -> Boolean> = mapOf(
//        AND to { subConds.map { it.state }.reduce { acc, c -> acc && c } },
//        OR to { subConds.map { it.state }.reduce { acc, c -> acc || c } },
//    )

    val state: Boolean by lazy {
        false
    }
}
