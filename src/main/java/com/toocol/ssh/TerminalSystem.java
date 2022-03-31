package com.toocol.ssh;

import cn.hutool.core.util.ClassUtil;
import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.CastUtil;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.configuration.vert.ConfigurationVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
public class TerminalSystem {

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    public static void main(String[] args) {
        PrintUtil.printTitle();
        PrintUtil.println("TerminalSystem register the vertx service.");

        if (args.length != 1) {
            PrintUtil.printErr("wrong boot parameter.");
            System.exit(-1);
        }

        ConfigurationVerticle.BOOT_TYPE = args[0];
        /* Get the verticle which need deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = ClassUtil.scanPackageByAnnotation("com.toocol.ssh.core", PreloadDeployment.class);
        List<Class<? extends AbstractVerticle>> preloadVerticleClassList = new ArrayList<>();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                preloadVerticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                PrintUtil.printErr("skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        final CountDownLatch initialLatch = new CountDownLatch(preloadVerticleClassList.size());

        /* Because need to establish SSH connections, increase the blocking check time */
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        Vertx vertx = Vertx.vertx(options);

        /* Set the final verticle to deploy */
        vertx.executeBlocking(future -> {
            Set<Class<?>> finalClassList = ClassUtil.scanPackageByAnnotation("com.toocol.ssh.core", FinalDeployment.class);
            finalClassList.forEach(finalVerticle -> {
                if (!finalVerticle.getSuperclass().equals(AbstractVerticle.class)) {
                    PrintUtil.printErr("skip deploy verticle " + finalVerticle.getName() + ", please extends AbstractVerticle");
                    return;
                }
                try {
                    boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
                    if (!ret) {
                        throw new RuntimeException();
                    }
                    vertx.deployVerticle(finalVerticle.getName(), complete -> future.complete());
                } catch (Exception e) {
                    PrintUtil.printErr("SSH TERMINAL START UP FAILED!!");
                    vertx.close();
                    System.exit(-1);
                }
            });
        }, res -> {
            try {
                PrintUtil.loading();
                vertx.eventBus().send(ADDRESS_ACCEPT_COMMAND.address(), true);
            } catch (Exception e) {
                PrintUtil.printErr("problem happened.");
                System.exit(-1);
            }
        });

        preloadVerticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(PreloadDeployment.class).weight()));
        preloadVerticleClassList.forEach(verticleClass ->
                vertx.deployVerticle(verticleClass.getName(), new DeploymentOptions(), result -> {
                    if (result.succeeded()) {
                        initialLatch.countDown();
                    } else {
                        PrintUtil.printErr("Terminal start up failed, verticle = " + verticleClass.getSimpleName());
                        vertx.close();
                        System.exit(-1);
                    }
                })
        );
    }
}
