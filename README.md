# Spring Transaction

## 트랜잭션 로그

```properties
logging.level.org.springframework.transaction.interceptor=TRACE
```

위의 로그를 추가하면, 트랜잭션 프록시가 호출하는 `트랜잭션의 시작` 과 `종료` 를 명확하게 로그로 확인할 수 있다.

## 트랜잭션 우선순위

**더 구체적이고, 자세한 것이 높은 우선순위를 가진다.** 따라서 클래스보다 메서드의 우선순위가 더 높다.

### @Transactional의 2가지 규칙

1. `우선순위 규칙`
2. `클래스에 적용하면, 메서드는 자동 적용`

### 인터페이스에 @Transactional 적용

1. 클래스의 메서드 (우선순위가 가장 높다.)
2. 클래스의 타입
3. 인터페이스의 메서드
4. 인터페이스의 타입 (우선수위가 가장 낮다.)

클래스의 메서드를 찾고, 없으면 클래스 타입을 찾고, 없으면 인터페이스의 메서드를 찾는다. 그래도 없으면 인터페이스의 타입을 찾는다.

- 인터페이스에 `@Transactional` 를 사용하는 것은 스프링 공식 메뉴얼에서 권장하지 않는 방법이다. **AOP를 적용하는 방식에 따라서 인터페이스의 AOP가 적용되지 않기 때문이다.**
  가급적이면, **구체 클래스에 `@Transactional` 을 적용하자!**

## 트랜잭션 AOP 주의 사항

### 프록시 내부 호출

보통 프록시 객체를 거치지 않고, 대상 객체를 바로 접근하는 순간에는 AOP가 적용되지 않는다. **대상 객체에서 내부 메서드에 대한 호출이 발생하면, 프록시를 거치지 않고 대상 객체의 메서드를 직접 호출하는 문제가
발생하게 된다.**

```java

@Slf4j
public class CallService {

    public void external() {
        log.info("call external");
        printTxInfo();
        internal();
    }

    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }

    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}
```

위의 코드에서 `external()` 메서드를 호출했을 때, 아래의 결과가 나온다.

<img width="788" alt="스크린샷 2022-06-12 오후 2 12 55" src="https://user-images.githubusercontent.com/23515771/173216250-1def31bc-21b3-4fa8-be26-76c144d88c8c.png">

즉, `internal()` 에 트랜잭션이 적용되지 않은 부분을 확인할 수 있다.

![스크린샷 2022-06-12 오후 2 09 00](https://user-images.githubusercontent.com/23515771/173215708-130e65ad-3e59-49a5-887c-962b9d3abaa4.png)

위의 그림을 토대로 프록시 내부 호출시 발생하는 문제를 알 수 있다. 자기 자신의 `internal()` 를 호출한다. 즉, `this.internal()` 메서드를 호출 한다.

### 프록시 내부 호출 개선하기

```java

@Slf4j
public class InternalService {

    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }

    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}
```

InternalService 클래스를 생성해서 `internal()` 메서드를 분리한다.

```java

@Slf4j
@RequiredArgsConstructor
public class CallService {

    private final InternalService internalService;

    public void external() {
        log.info("call external");
        printTxInfo();
        internalService.internal();
    }

    private void printTxInfo() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("tx active={}", txActive);
    }
}
```

CallService 클래스에는 `external()` 메서드만 존재하고, InternalService 클래스를 주입 받아서 `internal()` 메서드를 호출한다.

![스크린샷 2022-06-12 오후 2 33 17](https://user-images.githubusercontent.com/23515771/173217218-8ca23c8e-3684-48a6-b78c-941cefd54db0.png)

실제 호출되는 흐름은 위의 이미지처럼 동작되고, InternalService는 `AOP Proxy로 생성되어 스프링 컨테이너에 등록되고`, 해당 AOP Proxy는 실제 InternalService 클래스의
내부 `internal()` 메서드를 호출한다.

### 초기화 시점

스프링 초기화 시점에는 트랜잭션 AOP가 적용되지 않을 수 있다.

```java

@Slf4j
static class Hello {

    @PostConstruct
    @Transactional
    public void initV1() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init @PostConstruct tx active={}", txActive);
    }
}
```

<img width="749" alt="스크린샷 2022-06-12 오후 2 53 30" src="https://user-images.githubusercontent.com/23515771/173217744-bd62210f-c9fd-47d2-a865-1a17d4c32b76.png">

`@PostConstructor` 와 `@Transactional` 를 함께 사용하면, 트랜잭션이 적용되지 않는다. 왜냐하면, 초기화 코드가 먼저 호출되고 그 다음에 트랜잭션 AOP가 적용되기 때문이다.
따라서**초기화 시점에는 해당 메서드에서 트랜잭션을 획득할 수 없다.**

### 초기화 시점에 트랜잭션을 적용하는 방법

스프링이 초기화 작업(AOP Proxy 만들거나 기타 등등)이 끝난 후에 해당 메서드를 호출하면 된다.

```java

@Slf4j
static class Hello {

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initV2() {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init @EventListener(ApplicationReadyEvent.class) tx active={}", txActive);
    }
}
```

<img width="1006" alt="스크린샷 2022-06-12 오후 2 55 00" src="https://user-images.githubusercontent.com/23515771/173217799-4e33c86f-767e-4ac8-b96d-63b0aa2b2ec9.png">

`@EventListener(ApplicationReadyEvent.class)` 을 사용하면, 스프링이 초기화가 다 끝난 후에 트랜잭션을 시작할 수 있도록 해준다.

## 비즈니스 요구사항에서 고려할 점

### 정상

- 주문시 결제를 성공하면, 주문 데이터를 생성하고 결제 상태를 `완료` 로 변경한다.

### 시스템 예외

- 주문시 복구 불가능한 예외가 발생하면, 전체 데이터를 `롤백` 한다.

### 비즈니스 예외

- 주문시 결제 잔고가 부족하면, `주문 데이터를 저장` 하고 결제 상태를 `대기` 로 처리한다.
    - 주문 데이터 및 결제 상태를 `커밋` 한다는 것
    - 이러한 경우 **고객에게 잔고 부족을 알리고, 별도의 계좌로 입금하도록 안내한다.**
