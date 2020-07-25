package com.tinkoff.stocks.order

import com.tinkoff.stocks.util.isContainSmth
import ru.tinkoff.invest.openapi.models.orders.Operation
import java.math.BigDecimal

data class OrderTaskData(
    private val _timePoint: Long,
    private val _figi: String?,
    private val _lots: String?,
    private val _price: String?,
    private val _operation: Operation,
    private val _token: String?
) {
    val timePoint: Long = _timePoint
    val figi: String = _figi ?: ""
    val lots: Int = _lots?.toIntOrNull() ?: 0
    val price: BigDecimal = _price?.toBigDecimalOrNull() ?: BigDecimal(0)
    val operation = _operation
    val token = _token ?: ""

    val isValid = figi.isContainSmth()
            && lots > 0
            && price > BigDecimal(0)
            && token.isContainSmth()

}