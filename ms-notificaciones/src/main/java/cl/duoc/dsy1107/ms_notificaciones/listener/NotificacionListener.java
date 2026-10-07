package cl.duoc.dsy1107.ms_notificaciones.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.ms_notificaciones.event.PrestamoCreadoEvent;
import cl.duoc.dsy1107.ms_notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class NotificacionListener {
    
    private final NotificacionService notificacionService;

    @RabbitListener(queues = "notificaciones.queue")
    public void recibir(PrestamoCreadoEvent evento) {
        notificacionService.enviar(evento);
    }

}
