package hello.springtx.deadlock;

import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadLockRepository {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTxRequiresNew(DeadLockEntity deadLockEntity) {
        log.info("Repository - saveTxRequiresNew 호출");
        entityManager.persist(deadLockEntity);
        log.info("Repository - saveTxRequiresNew 종료");
    }

    @Transactional
    public void save(DeadLockEntity deadLockEntity) {
        log.info("Repository - save 호출");
        entityManager.persist(deadLockEntity);
        log.info("Repository - save 종료");
    }

    public Optional<DeadLockEntity> findBySituation(String situation) {
        return entityManager
            .createQuery(
                "select d from DeadLockEntity d where d.situation = :situation",
                DeadLockEntity.class
            )
            .setParameter("situation", situation)
            .getResultList()
            .stream().findAny();
    }
}
