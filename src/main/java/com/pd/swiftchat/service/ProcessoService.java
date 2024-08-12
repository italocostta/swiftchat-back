package com.pd.swiftchat.service;

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

    public List<Processo> getAllProcessos(){
        return processoRepository.findAll();
    }

    public Optional<Processo> getProcessoById(int id){
        return processoRepository.findById((long) id);
    }

    public Processo createProcesso(Processo processo){
        return processoRepository.save(processo);
    }

    public Processo updateProcesso(Long id, Processo processo){
        if (processoRepository.existsById(id)) {
            processo.setId(id);
            return processoRepository.save(processo);
        }
        return null;
    }

    public void deleteProcesso(int id){
        processoRepository.deleteById((long) id);
    }
}
