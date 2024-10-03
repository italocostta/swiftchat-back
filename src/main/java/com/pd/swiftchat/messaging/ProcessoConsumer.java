package com.pd.swiftchat.messaging;

import com.pd.swiftchat.config.RabbitMQConfig;
import com.pd.swiftchat.model.Processo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProcessoConsumer {

    @RabbitListener(queues = RabbitMQConfig.PROCESSO_TRAMITE_QUEUE)
    public void consumirProcessoTramite(Processo processo) {
        // Lógica para tratar processos em tramite
        System.out.println("Processo em tramite recebido: " + processo.getId());
    }

    @RabbitListener(queues = RabbitMQConfig.PROCESSO_INDEFERIDO_QUEUE)
    public void consumirProcessoIndeferido(Processo processo) {
        // Lógica para tratar processos indeferidos
        System.out.println("Processo indeferido recebido: " + processo.getId());
    }

    @RabbitListener(queues = RabbitMQConfig.PROCESSO_DEFERIDO_QUEUE)
    public void consumirProcessoDeferido(Processo processo) {
        // Lógica para tratar processos deferidos
        System.out.println("Processo deferido recebido: " + processo.getId());
    }
}
