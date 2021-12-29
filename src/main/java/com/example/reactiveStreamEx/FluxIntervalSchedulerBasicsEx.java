package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Flux 에서 subscribeOn 과 publishOn Operator 사용
 *
 */
@Slf4j
public class FluxIntervalSchedulerBasicsEx {
    public static void main(String[] args) throws InterruptedException {
        Flux.interval(Duration.ofMillis(500)) // demon Thread 생성 함으로 별도의 Thread Sleep 을 걸어야 실행됨
                .take(10) // 처음 부터 10개만
                .doOnNext(s -> log.info("onNext : " + s.toString()))
                .subscribe();

        log.info("exit");
        Thread.sleep(10000);
    }
}
