package com.pd.swiftchat.service;

import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.repository.SetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    public List<Setor> getAllSetores() {
        return setorRepository.findAll();
    }

    public Optional<Setor> getSetorById(Long id) {
        return setorRepository.findById(id);
    }

    public Setor createSetor(Setor setor) {
        return setorRepository.save(setor);
    }

    public Setor updateSetor(Long id, Setor setorAtualizado) {
        if (setorRepository.existsById(id)) {
            setorAtualizado.setId(id);
            return setorRepository.save(setorAtualizado);
        }
        return null;
    }

    public void deleteSetor(Long id) {
        setorRepository.deleteById(id);
    }

    public Setor getSetorPorTipoProcesso(TipoProcesso tipoProcesso) {

        if (tipoProcesso.getNome().equalsIgnoreCase("Licença de resíduos sólidos")) {
            return setorRepository.findById(2L).orElseThrow(() -> new RuntimeException("Setor não encontrado"));
        }

        throw new RuntimeException("Setor não encontrado para o tipo de processo: " + tipoProcesso.getNome());
    }
}
