package cl.duoc.dsy1107.ms_historial.listener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.dsy1107.ms_historial.service.HistorialService;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class HistorialListener {
    
    private final HistorialService historialService;

    @RabbitListener(queues = "historial.queue")
    public void recibir(Map<String, Object> evento) {
        historialService.registrar(evento);
    }

}
