package com.tinkoff.stocks

import com.tinkoff.stocks.order.OrderDateLooper
import com.tinkoff.stocks.order.OrderTaskData
import com.tinkoff.stocks.order.OrderTaskLooper
import com.tinkoff.stocks.util.toInfoString
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.header
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import ru.tinkoff.invest.openapi.models.orders.Operation
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio
import java.lang.StringBuilder
import java.util.logging.Logger

fun main() {

    var portfolio: Portfolio?

    val tinkoffApiWrapper = TinkoffApiWrapper()
    val orderTaskPool = OrderTaskLooper.instance(tinkoffApiWrapper)
    val orderDateLooper = OrderDateLooper.instance()

    val logger = Logger.getGlobal()

    val server = embeddedServer(Netty, port = 8080) {

        routing {

            get("/portfolio") {
                val token = call.request.header("x-auth") ?: ""

                val api = tinkoffApiWrapper.getAccessToTinkoffApi(token)
                val id = tinkoffApiWrapper.getAccountBrokerId(token)

                portfolio = api.portfolioContext.getPortfolio(id).join()

                call.respondText(portfolio?.toInfoString() ?: "", ContentType.Text.Plain)
            }
            get("/orderbook") {
//                val figi = call.parameters["figi"] ?: ""

//                val subscriber = OrderBookSubscriber(logger, Executors.newSingleThreadExecutor())

//                api = createAccessToTinkoffApi(token, logger)
//                api.streamingContext.eventPublisher.subscribe(subscriber)
//                api.streamingContext.sendRequest(StreamingRequest.subscribeOrderbook(figi, 10))

            }
            get("/orderBuy") {
                val token = call.request.header("x-auth") ?: ""

                val ticker = call.parameters["ticker"]
                val lots = call.parameters["lots"]
                val operation = Operation.Buy
                val price = call.parameters["price"]

                val api = tinkoffApiWrapper.getAccessToTinkoffApi(token)

                val figi =
                    api.marketContext.searchMarketInstrumentsByTicker(ticker ?: "").join()?.instruments?.get(0)?.figi

                val order = OrderTaskData(
                    _timePoint = orderDateLooper.getTimeByPoint(OrderDateLooper.OrderTimePoint.START_OF_PRE_MARKET),
                    _figi = figi,
                    _lots = lots,
                    _price = price,
                    _operation = operation,
                    _token = token
                )
                if (order.isValid) {
                    logger.info("=======================================================")
                    logger.info("order added: $order")
                    orderTaskPool.add(order)
                }
                call.respondText("Success")
            }
            get("/orderSell") {
                val token = call.request.header("x-auth") ?: ""

                val ticker = call.parameters["ticker"]
                val lots = call.parameters["lots"]
                val operation = Operation.Sell
                val price = call.parameters["price"]

                val api = tinkoffApiWrapper.getAccessToTinkoffApi(token)

                val figi =
                    api.marketContext.searchMarketInstrumentsByTicker(ticker ?: "").join()?.instruments?.get(0)?.figi

                val order = OrderTaskData(
                    _timePoint = orderDateLooper.getTimeByPoint(OrderDateLooper.OrderTimePoint.START_OF_PRE_MARKET),
                    _figi = figi,
                    _lots = lots,
                    _price = price,
                    _operation = operation,
                    _token = token
                )
                if (order.isValid) {
                    logger.info("=======================================================")
                    logger.info("order added: $order")
                    orderTaskPool.add(order)
                }
                call.respondText("Success")
            }
        }
    }
    server.start(wait = true)
}
