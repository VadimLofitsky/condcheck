package lofitsky.condcheck.pack.hp.service.impl

import com.desprice.springutils.Slf4jLogger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.medicbk.calcfunc.CalcScriptContext
import lofitsky.condcheck.getFieldsMap
import lofitsky.condcheck.logic.dsl.ConditionDslElement
import lofitsky.condcheck.model.DataObject
import lofitsky.condcheck.pack.hp.model.PatientThTypeCondCheckDataV0
import lofitsky.condcheck.pack.hp.sample.LogicDslHpThType0
import lofitsky.condcheck.service.LogicService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit


@Service(value = "logicThTypeV0Service")
class LogicThTypeV0ServiceImpl : LogicService {
    @Slf4jLogger
    private lateinit var logger: Logger

    @Autowired
    private lateinit var calcScriptContext: CalcScriptContext

    private fun runHpTask(patientId: Long, isHfRiskFactor: Boolean, isPrevTherapyCheck: Boolean): Pair<Boolean, Int> {
        logger.info("Запуск задачи gradle")
        val gradleTaskProcess = ProcessBuilder()
            .command("gradle", "calcDataForThTypeChoice", "-PpatientId=$patientId", "-PisHfRiskFactor=$isHfRiskFactor", "-PisPrevTherapyCheck=$isPrevTherapyCheck")
            .directory(File("/home/vadim/dev/repo/medicbk/hyper2"))
            .redirectErrorStream(true)
            .redirectOutput(File("/home/vadim/MyPrjs/kt/condcheck/log.txt").also { it.createNewFile() })
            .start()

        val isNotExpired = gradleTaskProcess.waitFor(3L, TimeUnit.MINUTES)
        val exitValue = gradleTaskProcess.exitValue()
        return isNotExpired to exitValue
    }

    private fun getPatientCondCheckData(patientId: Long, isHfRiskFactor: Boolean, isPrevTherapyCheck: Boolean): PatientThTypeCondCheckDataV0 {
        val (isNotExpired, exitValue) = runHpTask(patientId, isHfRiskFactor, isPrevTherapyCheck)

        if(!isNotExpired) {
            logger.error("Задача gradle остановилась по таймауту!");
            throw RuntimeException("Задача gradle остановилась по таймауту!")
        }
        if(exitValue != 0) {
            logger.error("Задача gradle закончилась не нормально: код $exitValue!");
            throw RuntimeException("Задача gradle закончилась не нормально: код $exitValue!")
        }

        logger.info("Обработка ответа задачи gradle")
        return File("/home/vadim/MyPrjs/kt/condcheck/patient_datas/patient${patientId}_data.txt")
            .readBytes()
            .let { jacksonObjectMapper().readValue<PatientThTypeCondCheckDataV0>(it) }
            .let { it.copy(selVarIds = it.selVarIds.sorted()) }
    }

    private fun getDslTree(patientCondCheckDataMap: Map<String, Any?>): ConditionDslElement? {
        val context = calcScriptContext.createContext()
        patientCondCheckDataMap.forEach {
            context.setVariable(it.key, it.value)
        }

        return LogicDslHpThType0.dsl.collect(context)
    }

    override fun getDataObject(patientId: Long, isHfRiskFactor: Boolean, isPrevTherapyCheck: Boolean): DataObject {
        val patientCondCheckData = getPatientCondCheckData(patientId, isHfRiskFactor, isPrevTherapyCheck)
        val sources = patientCondCheckData.getFieldsMap()
        return DataObject(
            sources = sources,
            dsl = getDslTree(sources),
        )
    }
}
