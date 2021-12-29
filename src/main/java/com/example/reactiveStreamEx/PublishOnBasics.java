package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reactive Stream Scheduler
 * [ publishOn ] Scheduler
 * onNext, onComplete, onError 실행시 적용하는 Scheduler
 * 데이터 생성하는 Publishing 는 대단히 빠른데  데이터를 처리하는 subscribe 가 느린경우 중간 Operator를 만들어 onNext, onError, OnEcomplete 다른 Thread로 처리
 * 주로 느린 consumer에 적용
 * ex ) flux.publishOn(Schedulers.single()).subscribe()
 * [ 실행 로그 ]
 * 17:48:27.339 [main] - onSubscribe
 * 17:48:27.341 [main] - request()
 * 17:48:27.342 [main] - exit
 * 17:48:27.342 [pool-1-thread-1] - onNext:1
 * 17:48:27.343 [pool-1-thread-1] - onNext:2
 * 17:48:27.343 [pool-1-thread-1] - onNext:3
 * 17:48:27.343 [pool-1-thread-1] - onNext:4
 * 17:48:27.343 [pool-1-thread-1] - onNext:5
 * 17:48:27.343 [pool-1-thread-1] - onComplete
 */
@Slf4j
public class PublishOnBasics {
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

        /** publishOn
         *  onNext, onComplete, onError 실행시 적용하는 Scheduler
         *  데이터 생성하는 Publishing 는 대단히 빠른데  데이터를 처리하는 subscribe 가 느린경우 중간 Operator를 만들어 onNext, onError, OnEcomplete 다른 Thread로 처리
         *  주로 느린 consumer에 적용
         *  ex ) flux.publishOn(Schedulers.single()).subscribe()
         *  아래의  Operator 에 의해서 별도의 Thread 로 처리
         *  구독되면서 처리되는 onSubscribe 와 requset main Thread에서
         *  나머지 onNext, onComplete, onError 는 별도의 Thread 로 처리
         */

        /** Operator
         * 위의 publisher 와 아래의 subscriber 를 연결하는 Operator ( 일종의 publisher 개념 )
         */
        Publisher<Integer> publishOnPublisher = sub -> {

            publisher.subscribe(new Subscriber<Integer>() {
                // 하나의 Thread 만 실행을 보장해주는 Thread pool, Thread 하나씩만 실행되고 나머지 Request 는 큐에 대기
                // Subscription 하나당 하나의 Thread 보장
                // Thread Core 에 단일 Thread 만 존재하는 것이 newSingleThreadExecutor
                ExecutorService es = Executors.newSingleThreadExecutor();

                @Override
                public void onSubscribe(Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(() -> sub.onNext(integer));
                }

                @Override
                public void onError(Throwable t) {
                    es.execute(() -> sub.onError(t));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(() -> sub.onComplete());
                    es.shutdown();
                }
            });

        };

        /** Subscriber
         *  구독된 데이터 처리
         */
        publishOnPublisher.subscribe(new Subscriber<Integer>() {
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
