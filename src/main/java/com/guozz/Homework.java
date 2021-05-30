package com.guozz;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 功能：作业
 *
 * by guozzdsg
 */
public class Homework {

    /**
     * 随机工具
     */
    private static final Random random = new Random();

    /**
     * 原子计数类，用来给线程池中的线程起名
     */
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    /**
     * 定义一个线程池
     */
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("homework - " + atomicInteger.getAndAdd(1));
        return thread;
    });


    /**
     * 启动方法
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Homework homework = new Homework();
        homework.method1();
        homework.method2();
        homework.method3();
        homework.method4();
        homework.method5();
        // 关闭线程池
        executorService.shutdown();
    }

    /**
     * 方式1：线程池使用future
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void method1() throws ExecutionException, InterruptedException {
        // 通过线程池提交一个带返回值得任务
        Future<Integer> result = executorService.submit(() -> {
            System.out.println("method1 thread : " + Thread.currentThread().getName());
            return this.getRandom();
        });
        System.out.println("method1 result  : " + result.get() + " , thread : " + Thread.currentThread().getName());
    }

    /**
     * 方式二：线程池执行futureTask
     */
    private void method2() {
        // 创建异步执行的任务
        FutureTask<Integer> futureTask = new FutureTask<Integer>(() -> {
            System.out.println("method2 thread : " + Thread.currentThread().getName());
            return random.nextInt(100);
        }) {
            // 异步任务执行完成，回调
            @Override
            protected void done() {
                try {
                    System.out.println("future.done():" + get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // 将异步执行的任务放到线程池中
        executorService.execute(futureTask);
        // 睡一秒等待子线程运行结束
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 获取结果
        try {
            System.out.println("method2 result  : " + futureTask.get() + " , thread : " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 方式三：completableFuture
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void method3() throws ExecutionException, InterruptedException {
        // 借助CompletableFuture 提交异步任务，并指定线程池
        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> {
            System.out.println("method3 thread : " + Thread.currentThread().getName());
            return this.getRandom();
        }, executorService);
        System.out.println("method3 result  : " + result.get() + " , thread : " + Thread.currentThread().getName());
    }

    /**
     * 通过countDownLatch控制等待
     *
     * @throws InterruptedException
     */
    private void method4() throws InterruptedException {
        // 创界一个接收结果的对象
        AtomicReference<Integer> result = new AtomicReference<>();
        // 创建计数器
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 提交计数器执行的任务
        executorService.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("method4 thread : " + Thread.currentThread().getName());
            result.set(this.getRandom());
            countDownLatch.countDown();
        });
        // 阻塞主线程，等待异步任务结束
        countDownLatch.await();
        System.out.println("method4 result  : " + result.get() + " , thread : " + Thread.currentThread().getName());
    }

    /**
     * 通过obj.wait让主线程等待子线程执行完
     *
     * @throws InterruptedException
     */
    private void method5() throws InterruptedException {
        // 定义一个锁对象
        Object lock01 = new Object();
        // 创建一个接收异步线程执行结果的对象
        AtomicReference<Integer> result = new AtomicReference<>();
        // 提交异步任务
        executorService.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock01) {
                System.out.println("method5 thread : " + Thread.currentThread().getName());
                // 设置结果
                result.set(this.getRandom());
                // 通知主线程可以接收结果了
                lock01.notify();
            }
        });
        synchronized (lock01) {
            // 阻塞主线程，并设置10秒超时
            lock01.wait(10000);
            System.out.println("method5 result  : " + result.get());
        }

    }

    /**
     * 获得一个随机数
     *
     * @return 随机数
     */
    private int getRandom() {
        return random.nextInt(100);
    }

}
