package com.tinkoff.stocks.util

import ru.tinkoff.invest.openapi.models.portfolio.Portfolio

fun Portfolio.toInfoString(): String {
    val pos = this.positions
    var info = ""
    pos.forEach { info = info.plus("$it\n") }
    return info
}