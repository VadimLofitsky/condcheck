package lofitsky.condcheck.controller

import com.desprice.springutils.Slf4jLogger
import lofitsky.condcheck.model.DataObject
import lofitsky.condcheck.service.LogicThTypeService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/rest")
class CondCheckRestController {
    @Slf4jLogger
    private lateinit var logger: Logger

    @Autowired
    private lateinit var logicThTypeService: LogicThTypeService

    @GetMapping
    fun main(@RequestParam("patientId") patientId: Long,
             @RequestParam("isHfRiskFactor") isHfRiskFactor: Boolean = false,
    ): DataObject {
        logger.info("*** Запрос: patientId=$patientId, isHfRiskFactor=$isHfRiskFactor")
        return logicThTypeService.getDataObject(patientId, isHfRiskFactor)
    }
}
