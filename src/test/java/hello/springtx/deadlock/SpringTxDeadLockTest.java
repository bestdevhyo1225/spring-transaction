package hello.springtx.deadlock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.CannotCreateTransactionException;

@Slf4j
@SpringBootTest
public class SpringTxDeadLockTest {

    @Autowired
    DeadLockService deadLockService;

    @Autowired
    DeadLockRepository deadLockRepository;

    /*
     * application.properties 설정
     *
     * server.tomcat.threads.min-spare=1
     * server.tomcat.threads.max=1
     *
     * spring.datasource.hikari.minimum-idle=1
     * spring.datasource.hikari.maximum-pool-size=1
     *
     * 위의 설정인 상태에서만 테스트 코드가 정상적으로 수행된다.
     * */

    @Test
    void success() {
        // given
        String situation = "정상적으로 작업을 수행한다.";

        // when
        deadLockService.save(situation);

        // then
        assertThat(deadLockRepository.findBySituation(situation).isPresent()).isTrue();
    }

    @Test
    void deadLock() {
        // given
        String situation = "데드락을 발생시킨다.";

        // when
        deadLockService.save(situation);

        // 테스트 환경에 맞게 tomcat, db connection pool 이 설정 되어 있어야 아래의 주석을 해제해서 테스트 할 수 있음
//        assertThatThrownBy(() -> deadLockService.save(situation))
//            .isInstanceOf(CannotCreateTransactionException.class);

        // then
//        assertThat(deadLockRepository.findBySituation(situation).isPresent()).isFalse();
        assertThat(deadLockRepository.findBySituation(situation).isPresent()).isTrue();
    }
}
