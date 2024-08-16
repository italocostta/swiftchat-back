package com.pd.swiftchat.service;

import com.pd.swiftchat.exception.CpfCnpjJaUtilizadoException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    public List<Processo> getAllProcessos() {
        return processoRepository.findAll();
    }

    public Optional<Processo> getProcessoById(Long id) {
        return processoRepository.findById(id);
    }

    public Processo createProcesso(Processo processo) {
        Optional<Processo> existingProcesso = processoRepository.findByCpf(processo.getCpf());
        if (existingProcesso.isPresent()) {
            throw new CpfCnpjJaUtilizadoException("CPF/CNPJ já utilizado em um processo. Aguarde a conclusão do mesmo.");
        }

        Optional<Processo> existingUsuarioProcesso = processoRepository.findFirstByUsuario(processo.getUsuario());
        if (existingUsuarioProcesso.isPresent()) {
            throw new RuntimeException("O usuário já possui um processo. Aguarde o deferimento ou indeferimento do mesmo.");
        }

        return processoRepository.save(processo);
    }

    public Processo updateProcesso(Long id, Processo processoAtualizado) {
        if (processoRepository.existsById(id)) {
            processoAtualizado.setId(id);
            return processoRepository.save(processoAtualizado);
        }
        return null;
    }

    public void deleteProcesso(Long id) {
        processoRepository.deleteById(id);
    }
}