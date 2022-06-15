package hello.springtx.deadlock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadLockService {

    private final DeadLockRepository deadLockRepository;

    @Transactional
    public void save(String situation) {
        log.info("Service - save 호출");
        DeadLockEntity deadLockEntity = DeadLockEntity.create(situation);

        if (situation.contains("데드락")) {
            deadLockRepository.saveTxRequiresNew(deadLockEntity);
        } else {
            deadLockRepository.save(deadLockEntity);
        }

        log.info("Service - save 종료");
    }
}
