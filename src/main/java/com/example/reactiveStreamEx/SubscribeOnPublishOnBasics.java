package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reactive Stream Scheduler
 * SubscribeOn 과 PublishOn 둘다 사용 Scheduler 예제
 * onSubscribe 와 request 는 SubscribeOn Scheduler에서 실행
 * onNext, onComplete, onError 는 publishOn Scheduler에서 실행 
 * [ 실행 로그 ]
 * 17:43:52.825 [main] - exit
 * 17:43:52.826 [subscribeOn-1] - onSubscribe
 * 17:43:52.828 [subscribeOn-1] - request()
 * 17:43:52.828 [publishOn-1] - onNext:1
 * 17:43:52.830 [publishOn-1] - onNext:2
 * 17:43:52.830 [publishOn-1] - onNext:3
 * 17:43:52.830 [publishOn-1] - onNext:4
 * 17:43:52.830 [publishOn-1] - onNext:5
 * 17:43:52.830 [publishOn-1] - onComplete
 */
@Slf4j
public class SubscribeOnPublishOnBasics {
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

        /**
         * subscribeOn Operator
         */
        Publisher<Integer> subscribeOnPublisher = sub -> {
            // 하나의 Thread 만 실행을 보장해주는 Thread pool, Thread 하나씩만 실행되고 나머지 Request 는 큐에 대기
            // Subscription 하나당 하나의 Thread 보장
            // Thread Core 에 단일 Thread 만 존재하는 것이 newSingleThreadExecutor
            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
                @Override
                public String getThreadNamePrefix() {
                    return "subscribeOn-";
                }
            });
            es.execute(() -> publisher.subscribe(sub));
        };

        /**
         * publishOn Operator
         */
        Publisher<Integer> publishOnPublisher = sub -> {

            subscribeOnPublisher.subscribe(new Subscriber<Integer>() {
                // 하나의 Thread 만 실행을 보장해주는 Thread pool, Thread 하나씩만 실행되고 나머지 Request 는 큐에 대기
                // Subscription 하나당 하나의 Thread 보장
                // Thread Core 에 단일 Thread 만 존재하는 것이 newSingleThreadExecutor
                ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory() {
                    @Override
                    public String getThreadNamePrefix() {
                        return "publishOn-";
                    }
                });

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
                }

                @Override
                public void onComplete() {
                    es.execute(() -> sub.onComplete());
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
