package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reactive Stream Scheduler
 * [ subscribeOn ] Scheduler
 * subscribe 실행시 onSubscribe & request 에 적용하는 Scheduler
 * 주로 느린 publisher, 특별히 데이터 생성시 Blocking I/O 를 사용하고 처리하는 consumer가 빠른 경우에 사용
 * ex ) flux.subscribeOn(Schedulers.single()).subscribe()
 * [ 실행 로그 ]
 * 17:46:55.708 [main] - exit
 * 17:46:55.708 [pool-1-thread-1] - onSubscribe
 * 17:46:55.710 [pool-1-thread-1] - request()
 * 17:46:55.710 [pool-1-thread-1] - onNext:1
 * 17:46:55.711 [pool-1-thread-1] - onNext:2
 * 17:46:55.711 [pool-1-thread-1] - onNext:3
 * 17:46:55.711 [pool-1-thread-1] - onNext:4
 * 17:46:55.711 [pool-1-thread-1] - onNext:5
 * 17:46:55.711 [pool-1-thread-1] - onComplete
 */
@Slf4j
public class SubscribeOnBasics {
    public static void main(String[] args) {

        /** Phublisher
         *  테이타 발생 Publisher
         */
        Publisher<Integer> publisher = sub -> {
          sub.onSubscribe(new Subscription() {
              @Override
              public void request(long n) {
                  log.debug("request()");
                  sub.onNext(1);
                  sub.onNext(2);
                  sub.onNext(3);
                  sub.onNext(4);
                  sub.onNext(5);
                  sub.onComplete();
              }

              @Override
              public void cancel() {

              }
          });
        };

        /** subscribeOn
         *  subscribe 실행시 onSubscribe & request 에 제공 적용하는 Scheduler
         *  주로 느린 publisher, 특별히 데이터 생성시 Blocking I/O 를 사용하고 처리하는 consumer가 빠른 경우에 사용
         *  ex ) flux.subscribeOn(Schedulers.single()).subscribe()
         *  아래의  Operator 에 의해서 별도의 Thread 로 처리
         *  구독되면서 처리되는 모든 Operating은 별도의 Thread 로 처리
         */

        /** Operator
         * 위의 publisher 와 아래의 subscriber 를 연결하는 Operator ( 일종의 publisher 개념 )
         */
        Publisher<Integer> subscribeOnPublisher = sub -> {
            // 하나의 Thread 만 실행을 보장해주는 Thread pool, Thread 하나씩만 실행되고 나머지 Request 는 큐에 대기
            // Subscription 하나당 하나의 Thread 보장
            // Thread Core 에 단일 Thread 만 존재하는 것이 newSingleThreadExecutor
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(() -> publisher.subscribe(sub));
        };

        /** Subscriber
         *  구독된 데이터 처리
         */
        subscribeOnPublisher.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                log.debug("onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("onNext:{}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });

        log.debug("exit");

    }
}
