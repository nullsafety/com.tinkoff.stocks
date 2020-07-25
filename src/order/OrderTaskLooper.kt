package com.tinkoff.stocks.order

import com.tinkoff.stocks.TinkoffApiWrapper
import ru.tinkoff.invest.openapi.models.orders.LimitOrder
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder
import ru.tinkoff.invest.openapi.models.orders.Status
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.logging.Logger

class OrderTaskLooper private constructor(private val tinkoffWrapper: TinkoffApiWrapper) {

    companion object {

        private var looper: OrderTaskLooper? = null

        @Synchronized
        fun instance(tinkoffWrapper: TinkoffApiWrapper): OrderTaskLooper {
            if (looper == null) {
                looper = OrderTaskLooper(tinkoffWrapper)
                looper?.execute()
            }
            return looper!!
        }
    }

    private val logger = Logger.getGlobal()

    private val dataExecutor = Executors.newSingleThreadExecutor()
    private val taskExecutor = Executors.newSingleThreadExecutor()

    private val taskDeque = ConcurrentLinkedDeque<() -> PlacedOrder>()
    private val dataDeque = ConcurrentLinkedDeque<OrderTaskData>()

    fun add(orderTaskData: OrderTaskData) {
        dataDeque.add(orderTaskData)
    }

    @Synchronized
    fun execute() {
        dataExecutor.submit {
            while (true) {
                if (dataDeque.isNotEmpty()) {
                    if (isDataForSubmit(dataDeque.first.timePoint)) {
                        taskDeque.add(createTask(dataDeque.first))
                        dataDeque.removeFirst()
                    }
                }
            }
        }
        taskExecutor.submit {
            while (true) {
                if (taskDeque.isNotEmpty()) {
                    val result = taskDeque.first.invoke()
                    if (result.status != Status.Rejected) {
                        taskDeque.removeFirst()
                    }
                    logger.info(result.toString())
                }
            }
        }
    }

    private fun isDataForSubmit(time: Long): Boolean {
        val calNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
        return calNow.timeInMillis >= time
    }

    private fun createTask(taskData: OrderTaskData): () -> PlacedOrder {
        return {
            val api = tinkoffWrapper.getAccessToTinkoffApi(taskData.token)
            val id = tinkoffWrapper.getAccountBrokerId(taskData.token)

            val calNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))

            logger.info("=======================================================")
            logger.info("cal now ${calNow.time}")

            api.ordersContext.placeLimitOrder(
                taskData.figi,
                LimitOrder(
                    taskData.lots,
                    taskData.operation,
                    taskData.price
                ),
                id
            ).whenComplete { placeOrder, throwable ->
                placeOrder?.let {
                    logger.info(it.toString())
                }
                throwable?.let {
                    logger.info(it.message)
                }
            }.join()
        }
    }
}