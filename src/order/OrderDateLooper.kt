package com.tinkoff.stocks.order

import java.util.*
import java.util.concurrent.Executors

class OrderDateLooper private constructor() {

    companion object {

        private var looper: OrderDateLooper? = null

        @Synchronized
        fun instance(): OrderDateLooper {
            if (looper == null) {
                looper = OrderDateLooper()
                looper?.run()
            }
            return looper!!
        }
    }

    enum class OrderTimePoint {
        NOW, START_OF_PRE_MARKET, START_OF_MARKET, START_OF_POST_MARKET
    }

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    var nowPoint: Long = 0

    @Volatile
    var startOfPreMarketPoint: Long = 0

    @Volatile
    var startOfMarketPoint: Long = 0

    @Volatile
    var startOfPostMarketPoint: Long = 0

    private fun run() {

        executor.submit {
            while (true) {
                nowPoint = System.currentTimeMillis()

//                val logger = Logger.getGlobal()
//                logger.info("==========================================")
//                logger.info(System.currentTimeMillis().toString())

                startOfPreMarketPoint = setTimeForCal(10, 0)
                startOfMarketPoint = setTimeForCal(16, 30)
                startOfPostMarketPoint = setTimeForCal(23, 0)

//                logger.info(startOfMarketPoint.toString())
//                logger.info(cl.time.toString())
            }
        }
    }

    private fun setTimeForCal(hour: Int, min: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))

        val calPoint = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
        ) {
            calPoint.set(Calendar.WEEK_OF_YEAR, calPoint.get(Calendar.WEEK_OF_YEAR) + 1)
            calPoint.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        if (cal.get(Calendar.HOUR_OF_DAY) > hour
            || (cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.MINUTE) > min)
        ) {
            calPoint.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) + 1)
        }
        calPoint.set(Calendar.HOUR_OF_DAY, hour)
        calPoint.set(Calendar.MINUTE, min)
        calPoint.set(Calendar.SECOND, 0)
        calPoint.set(Calendar.MILLISECOND, 1)
        return calPoint.timeInMillis
    }

    fun getTimeByPoint(timeType: OrderTimePoint): Long {
        return when (timeType) {
            OrderTimePoint.NOW -> nowPoint
            OrderTimePoint.START_OF_PRE_MARKET -> startOfPreMarketPoint
            OrderTimePoint.START_OF_MARKET -> startOfMarketPoint
            OrderTimePoint.START_OF_POST_MARKET -> startOfPostMarketPoint
        }
    }
}