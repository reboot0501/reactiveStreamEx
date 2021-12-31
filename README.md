# reactiveStreamEx
Reactive Streams Example

출처: https://moonsiri.tistory.com/76 [Just try it!]

Reactive Streams 이란 non-blocking backpressure를 이용하여 비동기 서비스를 할 때 기본이 되는 스펙
Java의 RxJava, String5 Webflux의 core에 있는 ProjectRector 프로젝터 모두 해당 스펙을 따르고 있음

reactive stream 시퀀스에서 소스 Publisher가 데이터를 생성
그러나 기본적으로 Subscriber가 등록(subscribe)할 때까지 아무 작업도 수행되지 않으며, 이때 데이터를 push

[ 명세서 ]

1. Publisher 
   : Publisher는 Subscriber를 받아들이는 메서드를 가짐

public interface Publisher<T> { 
  public void subscribe(Subscriber<? super T> s); 
}

2. Subscriber
  : Subscriber는 Subscription을 등록하고 Subscription에서 오는 신호(onNext, onError, onComplete)에 따라서 동작

public interface Subscriber<T> { 
  public void onSubscribe(Subscription s); 
  public void onNext(T t); 
  public void onError(Throwable t); 
  public void onComplete(); 
}

3. Subscription
  : Subscription은 Publisher와 Subscriber 사이에서 중계하는 역할을 담당. 
    request 메서드는 Subscriber가 Publisher에게 데이터를 요청하는 개수이며 cancel 메서드는 구독을 취소하겠다는 의미

[ 동작 ]

1. Publisher에 본인이 소유할 Subscription을 구현하고 publishing 할 data를 생성
2. Publisher는 subscribe() 메서드를 통해 subscriber를 등록
3. Subscriber는 onSubscribe() 메서드를 통해 Subscription을 등록하고 Publisher를 구독하기 시작
   이는 Publisher에 구현된 Subscription을 통해 이루어집니다. 
   이렇게 하면 Publisher와 Subscriber는 Subscription을 통해 연결된 상태가 됩니다. 
   onSubscribe() 내부에 Subscription의 request()를 요청하면 그때부터 data 구독이 시작됩니다.
4. Suscriber는 Subscription 메서드의 request() 또는 cancel()을 호출을 통해 data의 흐름을 제어할 수 있습니다.
5. Subscription의 request()에는 조건에 따라 Subscriber의 onNext(), onComplete() 또는 onError()를 호출합니다. 
   그러면 Subscriber의 해당 메서드의 로직에 따라 request() 또는 cancel()로 제어하게 됩니다.






