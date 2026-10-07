package cl.duoc.dsy1107.ms_notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cl.duoc.dsy1107.ms_notificaciones.event.PrestamoCreadoEvent;

@Service 
public class NotificacionService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void enviar(PrestamoCreadoEvent evento) {
        //Simualdo: en un sistema real se envia un correo o push
        log.info("Notificacion enviada a {}: préstamo {} de libro {} creado (devolver antes del {})",
            evento.usuario(), evento.prestamoId(), evento.libroId(), evento.fechaDevolucionEstimada());
    }


}
