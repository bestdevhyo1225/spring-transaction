# Spring Transaction

# 트랜잭션 로그

```properties
logging.level.org.springframework.transaction.interceptor=TRACE
```

위의 로그를 추가하면, 트랜잭션 프록시가 호출하는 `트랜잭션의 시작` 과 `종료` 를 명확하게 로그로 확인할 수 있다.
