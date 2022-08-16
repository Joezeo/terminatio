package com.toocol.termio.console.module;

import com.toocol.termio.console.handlers.DynamicEchoHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 23:52
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolSize = 1, workerPoolName = "term-dynamic-worker-pool")
@RegisterHandler(handlers = {
        DynamicEchoHandler.class
})
public final class DynamicEchoModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}