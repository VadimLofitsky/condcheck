package lofitsky.condcheck.model

import com.medicbk.calcfunc.FieldValue
import kotlin.reflect.full.declaredMemberProperties

data class PatientCondCheckData(
    val fieldsToSkip: List<String>,
    val grade: Int,
    val score: Int,
    val scoreByRisk: Int,
    val isHfRiskFactor: Boolean,
    val selVarIds: List<Long>,
    val fieldValues: List<FieldValue>,
    val risk: Int,
    val riskLow: Int,
    val riskMed: Int,
    val riskHigh: Int,
    val riskVHigh: Int,
    val riskExtreme: Int,
    val scoreMedLowerBound: Int,
    val scoreHighLowerBound: Int,
    val ifTargetBpsReached: Boolean,
    val prevTherapyIsGood: Boolean,
    val isPrevTherapyCheck: Boolean,
    val age: Int,
    val sbp: Int,
    val dbp: Int,
    val sadMax: Int,
    val dadMax: Int,
    val additionalLists: List<List<PatientCondCheckDataComb>>,
) {
    fun _getFieldsMap(): Map<String, Any>
        = this::class.declaredMemberProperties
            .associate { it.name to it.getter.call(this) }
            .mapValues { it.value ?: Any() }
}

data class PatientCondCheckDataComb(
    val subgroupName: String,
    val combId: Long,
    val name: String,
)

