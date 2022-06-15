package hello.springtx.deadlock;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class DeadLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String situation;

    protected DeadLockEntity() {
    }

    private DeadLockEntity(String situation) {
        this.situation = situation;
    }

    public static DeadLockEntity create(String situation) {
        return new DeadLockEntity(situation);
    }
}
