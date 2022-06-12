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
