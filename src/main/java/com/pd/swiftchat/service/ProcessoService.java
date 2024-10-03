package com.pd.swiftchat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.SetorRepository;
import com.pd.swiftchat.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper; // para converter para JSON

    // Método para funcionários obterem todos os processos
    public List<Processo> getAllProcessos() {
        return processoRepository.findAll();
    }

    // Método para atualizar o status do processo e enviar para a fila apropriada
    public Processo avaliarProcesso(Long id, String statusProcesso, String observacao) {
        Processo processo = getProcessoById(id).orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        String statusAnterior = processo.getStatusProcesso();
        processo.setStatusProcesso(statusProcesso);
        processo.setObservacao(observacao);

        // Remover da fila atual (Em tramitação)
        if ("Em tramitação".equals(statusAnterior)) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PROCESSO_TRAMITE_QUEUE, "REMOVE", processo);
        }

        // Enviar para a fila de acordo com o novo status
        switch (statusProcesso) {
            case "DEFERIDO":
                rabbitTemplate.convertAndSend(RabbitMQConfig.PROCESSO_DEFERIDO_QUEUE, processo);
                break;
            case "INDEFERIDO":
                rabbitTemplate.convertAndSend(RabbitMQConfig.PROCESSO_INDEFERIDO_QUEUE, processo);
                break;
            case "EM_TRAMITE":
            default:
                rabbitTemplate.convertAndSend(RabbitMQConfig.PROCESSO_TRAMITE_QUEUE, processo);
                break;
        }

        return processoRepository.save(processo);
    }

    // Método para um usuário comum (física ou jurídica) obter seus próprios processos
    public List<Processo> getProcessosByUsuario(UserDetails userDetails) {
        String cpfOrCnpj = userDetails.getUsername();

        if (cpfOrCnpj.length() == 11) {
            // CPF - Pessoa Física
            return processoRepository.findByCpf(cpfOrCnpj);
        } else if (cpfOrCnpj.length() == 14) {
            // CNPJ - Pessoa Jurídica
            return processoRepository.findByCnpj(cpfOrCnpj);
        } else {
            throw new IllegalArgumentException("Identificador de usuário inválido: " + cpfOrCnpj);
        }
    }

    // Método que decide se um usuário comum ou funcionário está chamando a listagem de processos
    public List<Processo> getAllProcessosByRole(Usuario usuarioLogado) {
        if (usuarioLogado.isFuncionario()) {
            // Funcionário pode ver os processos de seu setor
            return getProcessosBySetor(usuarioLogado);
        } else {
            // Usuário comum só pode ver seus próprios processos
            return getProcessosByUsuario(usuarioLogado);
        }
    }

    public Optional<Processo> getProcessoById(Long id) {
        return processoRepository.findById(id);
    }

    // Método para acessar um processo específico, respeitando regras de acesso
    public Processo getProcessoById(Long id, UserDetails userDetails) throws ResourceNotFoundException {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado com ID: " + id));

        // Usuário só pode acessar seu próprio processo, funcionário pode acessar qualquer um
        if (!userDetails.getUsername().equals(processo.getCpf()) && !isFuncionario(userDetails)) {
            throw new AccessDeniedException("Você não tem permissão para acessar este processo.");
        }

        return processo;
    }

    // Criação de um processo com a atribuição automática de setor intermediário e envio para a fila RabbitMQ
    public Processo createProcesso(Processo processo) {
        Optional<Setor> setorIntermediario = setorRepository.findByNome("Setor Intermediario");
        if (setorIntermediario.isPresent()) {
            processo.setSetor(setorIntermediario.get());
        } else {
            throw new RuntimeException("Setor Intermediário não encontrado.");
        }

        // Certifique-se de que o status inicial seja 'Em Tramitação'
        if (processo.getStatusProcesso() == null) {
            processo.setStatusProcesso("Em Tramitação");
        }

        Processo novoProcesso = processoRepository.save(processo);

        // Enviar o processo recém-criado para a fila de tramitação
        rabbitTemplate.convertAndSend(RabbitMQConfig.PROCESSO_TRAMITE_QUEUE, novoProcesso);

        return novoProcesso;
    }

    // Atualização de um processo existente
    public Processo updateProcesso(Long id, Processo processoAtualizado) {
        if (processoRepository.existsById(id)) {
            processoAtualizado.setId(id);
            return processoRepository.save(processoAtualizado);
        }
        return null;
    }

    // Deleção de um processo por ID
    public void deleteProcesso(Long id) {
        processoRepository.deleteById(id);
    }

    // Verifica se o usuário é um funcionário
    public boolean isFuncionario(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("FUNCIONARIO"));
    }

    // Método para funcionários obterem processos de seu setor
    public List<Processo> getProcessosBySetor(Usuario funcionario) {
        Setor setorFuncionario = funcionario.getSetor();
        return processoRepository.findBySetor(setorFuncionario);
    }

}
