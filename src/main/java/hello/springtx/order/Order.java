package hello.springtx.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String username; // 정상, 예외, 잔고부족

    private String payStatus; // 대기, 완료

    public Order(String username) {
        this.username = username;
    }

    public void verifyIsValid() throws NotEnoughMoneyException {
        if (username.equals("예외")) {
            throw new RuntimeException("시스템 예외");
        }

        if (username.equals("잔고부족")) {
            changePayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다.");
        }
    }

    public void changePayStatus(String value) {
        if (!value.equals("대기") && !value.equals("완료")) {
            throw new IllegalArgumentException("유효하지 않은 결제 상태를 전달 받았습니다.");
        }
        payStatus = value;
    }
}
