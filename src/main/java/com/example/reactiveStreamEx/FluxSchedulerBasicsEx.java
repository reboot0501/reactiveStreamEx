package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Flux 에서 subscribeOn 과 publishOn Operator 사용
 *
 */
@Slf4j
public class FluxSchedulerBasicsEx {
    public static void main(String[] args) {
        Flux.range(1, 10)
                .publishOn(Schedulers.newSingle("publishOn"))
                .log()
                .subscribeOn(Schedulers.newSingle("subscribeOn"))
                .doOnNext(s -> log.info("결과 : {}", s))
                .subscribe();

        log.info("exit");
    }
}
