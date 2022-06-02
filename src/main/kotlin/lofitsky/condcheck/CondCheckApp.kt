package lofitsky.condcheck

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener


@SpringBootApplication
class CondCheckApp {
    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        Runtime.getRuntime().exec("./openpage.sh v0")
    }
}

fun main(args: Array<String>) {
    runApplication<CondCheckApp>(*args)
}
