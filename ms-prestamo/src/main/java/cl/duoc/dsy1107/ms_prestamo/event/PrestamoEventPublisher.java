package cl.duoc.dsy1107.ms_prestamo.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.ms_prestamo.config.RabbitConfig;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class PrestamoEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;

    public void publicarCreado(PrestamoCreadoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_CREADO, evento);
    }

    public void publicarDevuelto(PrestamoDevueltoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_DEVUELTO, evento);
    }

}
