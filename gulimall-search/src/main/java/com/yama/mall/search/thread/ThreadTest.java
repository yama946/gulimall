package com.yama.mall.search.thread;

import java.util.concurrent.*;

/**
 * TODO 异步编排代码示例
 * @description:
 * @date: 2022年10月02日 周日 7:29
 * @author: yama946
 */
public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //没有返回值
        /*System.out.println("main.......start.......");
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程："+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("执行结果："+i);
        }, executor);
        System.out.println("main.......end.......");*/
        //存在返回值
        /*System.out.println("main.......start.......");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("执行结果：" + i);
            return i;
        }, executor).whenComplete((result,except)->{
            //可以感知异常，但是无法处理异常并返回数据
            System.out.println("异步任务执行结果："+result+";异步任务执行异常："+except);
        }).exceptionally(t->{
            t.printStackTrace();//打印异常
            return 12;
        });*/
        /**
         * future.get()方法会阻塞，如果future没有执行结束，该方法不会执行。
         */
//        System.out.println("main.......end......."+future.get());
        /**
         * 线程串行化
         * 方式1：thenRun(),不能获取上一步返回结果，无返回值
         * 方式2：thenAccept(),能获取上一步的返回结果，无法返回值
         * 方式3：thenApply(),能获取上一步的返回结果，并返回值
         */
        /*System.out.println("main.......start.......");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("执行结果：" + i);
            return i;
        }, executor).thenApplyAsync(t->{
            System.out.println("任务2启动了.......");
            return t*3;
        },executor);*/
//        System.out.println("main.......end......."+future.get());
        /**
         * 任务组合完成
         */
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1执行开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1执行结束：" + i);
            return i;
        }, executor);

        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2执行开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务2执行结束：" + i);
            return i*3;
        }, executor);

        future01.runAfterBothAsync(future02,()->{
            System.out.println("任务3执行");
        },executor);

        executor.shutdown();
    }
}
