package com.example.reactiveStreamEx;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IntervalBasicsEx {
    public static void main(String[] args) {
        /**
         * Publisher
         */
        Publisher<Integer> publisher = sub -> {
            sub.onSubscribe(new Subscription() {
                int no = 0;
                boolean canceled = false;

                @Override
                public void request(long n) {
                    // 일정시간 간격두고 작업 수행
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        if (canceled) {
                            exec.shutdown();
                            return;
                        }
                        sub.onNext(no++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    canceled = true;
                }
            });
        };

        /**
         * take Operator
         */
        Publisher<Integer> takePublisher = sub -> {
            publisher.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription subsc;

                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    sub.onNext(integer);
                    if (++count >= 5) {
                        subsc.cancel();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };

        /** Subscriber
         *  구독된 데이터 처리
         */
        takePublisher.subscribe(new Subscriber<Integer>() {
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
