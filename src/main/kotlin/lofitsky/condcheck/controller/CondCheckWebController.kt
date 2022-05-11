package lofitsky.condcheck.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/")
class CondCheckWebController {
    @GetMapping
    fun main(): String {
        return "main.html"
    }

    @GetMapping("/v0")
    fun v0Page(): String {
        return "condcheck0.html"
    }

    @GetMapping("/v1")
    fun v1Page(): String {
        return "condcheck1.html"
    }
}
