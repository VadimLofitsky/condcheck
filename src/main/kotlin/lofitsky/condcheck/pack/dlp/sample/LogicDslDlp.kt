package lofitsky.condcheck.pack.dlp.sample

import lofitsky.condcheck.logic.dsl.Builder
import lofitsky.condcheck.logic.dsl.and
import lofitsky.condcheck.logic.dsl.or
import lofitsky.condcheck.logic.dsl.p
import lofitsky.condcheck.logic.sample.LogicDsl


object LogicDslDlp : LogicDsl() {
    private val builder = Builder()

    private val cond1 = p("cond1: пред.терапия == Нет", "#selVarIds.contains(212L)")
    private val cond2 = p("cond2: пред.терапия <> Нет", "!#selVarIds.contains(212L)")

    private val cond3 = and("cond3: Соответствует КР") {
            p("пред.терапия +КР +ЦП", "#prevTherapyIsGood")
            p("не проверка пред.терапии", "!#isPrevTherapyCheck")
        }

    private val cond4 = and("cond4: норм.выс. АД и В/ОчВ/Экстр риск") {
            and("норм. высокое АД") {
                p("САД >= 130", "#sbp >= 130")
                p("САД < 140", "#sbp < 140")
                p("ДАД >= 85", "#dbp >= 85")
                p("ДАД < 90", "#dbp < 90")
            }

            or("В/ОчВ/Экстр риск") {
                p("score >= 10", "#score >= #scoreHighLowerBound")
                p("высокий риск", "#risk == #riskHigh")
                p("очень высокий риск", "#risk == #riskVHigh")
                p("экстрем. высокий риск", "#risk == #riskExtreme")
            }
        }

    private val cond5 = and("cond5: АД < 150/90 и низкий риск") {
            p("САД < 150", "#sbp < 150")
            p("ДАД < 90", "#dbp < 90")
            p("score < 5", "#score < #scoreMedLowerBound")
            p("низкий риск", "#risk == #riskLow")
        }

    private val cond6 = and("cond6: grade == 1 и (В/ОчВ/Экстр риск или ПОМ)") {
            p("grade == 1", "#grade == 1")

            or("В/ОчВ/Экстр риск или ПОМ") {
                p("очень высокий score", "#score >= #scoreHighLowerBound")
                p("высокий риск", "#risk == #riskHigh")
                p("очень высокий риск", "#risk == #riskVHigh")
                p("экстрем. высокий риск", "#risk == #riskExtreme")
                p("поражение ОМ (АГ)", "@calcFunc.hasTargetOrganDamageHpVersionForThtLogic(#fieldValues)")
            }
        }

    private val cond7 = and("cond7") {
            p("grade >= 2", "#grade >= 2")
            p("возраст >= 80", "#age >= 80")
        }

//      cond8 text default ''; -- условие со старческой астенией

    private val cond9 = and("cond9: (САД=150..159 или ДАД=90..99) и низкий/умеренный риск") {
            or("САД=150..159 или ДАД=90..99") {
                and("") {
                    p("САД >= 150", "#sbp >= 150")
                    p("САД < 160", "#sbp < 160")
                }

                and("") {
                    p("ДАД >= 90", "#dbp >= 90")
                    p("ДАД < 99", "#dbp < 99")
                }
            }

            p("score < 10", "#score < #scoreHighLowerBound")

            or("риск low/med") {
                p("риск низкий", "#risk == #riskLow")
                p("риск умеренный", "#risk == #riskMed")
            }
        }

    private val cond10 = p("cond10: grade >= 2", "#grade>=2")

    override val dsl = builder.appendDsl {
        or {
            // cond4 || ' || ' || cond5 || ' || ' || cond6
            or("ОЖ") {
                this+ cond4
                this+ cond5
                this+ cond6
            }

            // (' || cond3 || ' && #selVarIds.contains(516L)) || (' || cond1 || ' && (' || cond4 || ' || ' || cond5 || ' || ' || cond7 || ')) || #isPrevTherapyCheck
            or("моно") {
                and {
                    this+ cond3
                    p("пред.терапия = моно", "#selVarIds.contains(516L)")
                }

                and {
                    this+ cond1
                    or {
                        this+ cond4
                        this+ cond5
                        this+ cond7
                    }
                }

                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }

/*
            // cond6 || ' || ' || cond9 || ' || ' || cond10 || ' || @calcFunc.any(#selVarIds, {213L, 516L, 517L}) || #isPrevTherapyCheck
            or("двойная") {
                this+cond6
                this+cond9
                this+cond10

                p("пред.терапия = другая/моно/двойная", "@calcFunc.any(#selVarIds, {213L, 516L, 517L})")
                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }
*/
            // (@calcFunc.any(#selVarIds, {213L, 516L, 517L}) && (cond6 || cond9 || cond10)) || @calcFunc.any(#selVarIds, {213L, 516L, 517L}) || #isPrevTherapyCheck
            // (@calcFunc.any(#selVarIds, {516L, 517L}) && (' || cond6 || ' || ' || cond9 || ' || ' || cond10 || ')) || #selVarIds.contains(213L) || #isPrevTherapyCheck
            or("двойная") {
                and {
                    p("пред.терапия = нет/моно/двойная", "@calcFunc.any(#selVarIds, {212L, 516L, 517L})")

                    or {
                        this+ cond6
                        this+ cond9
                        this+ cond10
                    }
                }

                p("пред.терапия = другая", "#selVarIds.contains(213L)")
                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }

            // (' || cond3 || ' && #selVarIds.contains(518L)) || #subgroupId==4 || @calcFunc.any(#selVarIds, {213L, 517L, 518L, 519L}) || #isPrevTherapyCheck
            or("тройная") {
                and {
                    this+ cond3
                    p("пред.терапия = тройная", "#selVarIds.contains(518L)")
                }

                p("ФР = СН", "#subgroupId == 4")
                p("пред.терапия = дргуая/двойная/тройная/тройная+", "@calcFunc.any(#selVarIds, {213L, 517L, 518L, 519L})")
                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }

            // (' || cond3 || ' && #selVarIds.contains(519L)) || @calcFunc.any(#selVarIds, {213L, 518L, 519L}) || #isPrevTherapyCheck
            or("тройная+") {
                and {
                    this+ cond3
                    p("пред.терапия = тройная+", "#selVarIds.contains(519L)")
                }

                p("пред.терапия = дргуая/тройная/тройная+", "@calcFunc.any(#selVarIds, {213L, 518L, 519L})")
                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }

            or("резистентная") {
                p("пред.терапия = дргуая/тройная+", "@calcFunc.any(#selVarIds, {213L, 519L})")
                p("проверка пред.терапии", "#isPrevTherapyCheck")
            }
        }
    }
}
