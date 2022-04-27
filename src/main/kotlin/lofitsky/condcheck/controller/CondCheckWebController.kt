package lofitsky.condcheck.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/")
class CondCheckWebController {
    @GetMapping
    fun main(): String {
        return "condcheck.html"
    }
}
