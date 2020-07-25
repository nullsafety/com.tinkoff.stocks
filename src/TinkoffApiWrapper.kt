package com.tinkoff.stocks

import ru.tinkoff.invest.openapi.OpenApi
import ru.tinkoff.invest.openapi.SandboxOpenApi
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory
import java.util.concurrent.Executors
import java.util.logging.Logger

class TinkoffApiWrapper {

    private var brokerAccountId: String? = null

    private val logger = Logger.getGlobal()

    private val sandBox = false

    private val apiMap = hashMapOf<String, OpenApi>()

    @Synchronized
    fun getAccessToTinkoffApi(token: String): OpenApi {
        apiMap[token]?.let { return it }
        val factory = OkHttpOpenApiFactory(token, logger)

        apiMap[token] = if (sandBox) {
            factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor()).apply {
                this.sandboxContext.performRegistration(null).join()
            }
        } else {
            factory.createOpenApiClient(Executors.newSingleThreadExecutor())
        }

        return apiMap[token]!!
    }

    fun getAccountBrokerId(token: String): String {
        brokerAccountId?.let { return it }
        apiMap[token]?.let { brokerAccountId = it.userContext.accounts.get().accounts[0].brokerAccountId }
        return brokerAccountId ?: ""
    }
}