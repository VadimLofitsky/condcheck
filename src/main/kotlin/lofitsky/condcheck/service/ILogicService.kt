package lofitsky.condcheck.service

import lofitsky.condcheck.model.DataObject


interface ILogicService {
    fun getDataObject(patientId: Long, isHfRiskFactor: Boolean, isPrevTherapyCheck: Boolean): DataObject
}
