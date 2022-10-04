package com.toocol.termio.utilities.module

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:49
 * @version: 0.0.1
 */
interface SuspendApi : ApiAcquirer, Castable, Loggable

interface ApiAcquirer {
    suspend fun <T : SuspendApi, R> CoroutineScope.api(
        api: T,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend T.() -> R
    ) : R {
        return withContext(context) {
            block(api)
        }
    }
}