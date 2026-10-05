package cl.duoc.dsy1107.ms_prestamo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration 
public class RabbitConfig {
    
    @Bean
    public ApplicationRunner abirConexion(ConnectionFactory connectionFactory) {
        return args -> connectionFactory.createConnection().close();
    }

    public static final String EXCHANGE = "biblioteca.events";
    public static final String QUEUE_NOTIFICACIONES = "notificaciones.queue";
    public static final String QUEUE_HISTORIAL = "historial.queue";
    public static final String RK_CREADO = "prestamo.creado";
    public static final String RK_DEVUELTO = "prestamo.devuelto";
    public static final String RK_PATRON = "prestamo.*";
    
    @Bean 
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean 
    public Queue notificacionesQueue() {
        return new Queue(QUEUE_NOTIFICACIONES, true);
    }

    @Bean 
    public Queue historialQueue() {
        return new Queue(QUEUE_HISTORIAL, true);
    }

    @Bean 
    public Binding bindingNotificaciones(Queue notificacionesQueue, TopicExchange exchange){
        return BindingBuilder.bind(notificacionesQueue).to(exchange).with(RK_CREADO);
    }

    @Bean 
    public Binding bindingHistorial(Queue historialQueue, TopicExchange exchange) {
        return BindingBuilder.bind(historialQueue).to(exchange).with(RK_PATRON);
    }

    @Bean 
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter(); //los msg viajan como JSON
    }
}
