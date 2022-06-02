package lofitsky.condcheck.service

import lofitsky.condcheck.model.DataObject


interface LogicService {
    fun getDataObject(patientId: Long, isHfRiskFactor: Boolean, isPrevTherapyCheck: Boolean): DataObject
}
