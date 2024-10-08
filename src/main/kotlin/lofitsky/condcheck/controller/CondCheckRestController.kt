package lofitsky.condcheck.controller

import com.desprice.springutils.Slf4jLogger
import lofitsky.condcheck.model.DataObject
import lofitsky.condcheck.service.ILogicService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/rest")
class CondCheckRestController {
    @Slf4jLogger
    private lateinit var logger: Logger

    @Autowired @Qualifier("thTypeV0Service") private lateinit var logicThTypeV0Service: ILogicService
    @Autowired @Qualifier("thTypeV1Service") private lateinit var logicThTypeV1Service: ILogicService

    @GetMapping("v0")
    fun processV0(@RequestParam("patientId") patientId: Long,
             @RequestParam("isHfRiskFactor") isHfRiskFactor: Boolean? = false,
             @RequestParam("isPrevTherapyCheck") isPrevTherapyCheck: Boolean? = false,
    ): DataObject {
        logger.info("*** Запрос: patientId=$patientId, isHfRiskFactor=$isHfRiskFactor, isPrevTherapyCheck=$isPrevTherapyCheck")
        return logicThTypeV0Service.getDataObject(patientId, isHfRiskFactor!!, isPrevTherapyCheck!!)
    }

    @GetMapping("v1")
    fun processV1(@RequestParam("patientId") patientId: Long,
             @RequestParam("isHfRiskFactor") isHfRiskFactor: Boolean? = false,
             @RequestParam("isPrevTherapyCheck") isPrevTherapyCheck: Boolean? = false,
    ): DataObject {
        logger.info("*** Запрос: patientId=$patientId, isHfRiskFactor=$isHfRiskFactor, isPrevTherapyCheck=$isPrevTherapyCheck")
        return logicThTypeV1Service.getDataObject(patientId, isHfRiskFactor!!, isPrevTherapyCheck!!)
    }
}
