package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order 호출");
        orderRepository.save(order);
        log.info("결제 프로세스 진입");
        order.verifyIsValid();
        log.info("정상 승인");
        order.changePayStatus(OrderPayStatus.COMPLETE);
        log.info("결제 프로세스 완료");
    }
}
