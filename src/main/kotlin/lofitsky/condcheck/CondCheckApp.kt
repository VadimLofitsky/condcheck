package lofitsky.condcheck

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class CondCheckApp
//class CondCheckApp : CommandLineRunner {
//    companion object {
//        fun main(args: Array<String>) {
//            runApplication<CondCheckApp>(*args)
//        }
//    }
//
//    override fun run(vararg args: String?) {
//        Runtime.getRuntime().exec("""xdg-open "http://localhost:9998/"""")
//    }
//}

fun main(args: Array<String>) {
        Runtime.getRuntime().exec("""xdg-open "http://localhost:9998/main"""")
    runApplication<CondCheckApp>(*args)
}
