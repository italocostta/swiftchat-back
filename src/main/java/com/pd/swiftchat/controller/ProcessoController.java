package com.pd.swiftchat.controller;

import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/processos")
public class ProcessoController {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @GetMapping
    public List<Processo> listarProcessos() {
        return processoRepository.findAll();
    }

    @PostMapping
    public Processo criarProcesso(@RequestBody Processo processo) {
        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processo.getTipoProcesso().getId());
        if (tipoProcesso.isPresent()) {
            processo.setTipoProcesso(tipoProcesso.get());
            return processoRepository.save(processo);
        } else {
            throw new RuntimeException("Tipo de Processo não encontrado");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Processo> obterProcesso(@PathVariable Long id) {
        Optional<Processo> processo = processoRepository.findById(id);
        return processo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Processo> atualizarProcesso(@PathVariable Long id, @RequestBody Processo processoAtualizado) {
        Optional<Processo> processoExistente = processoRepository.findById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setNome(processoAtualizado.getNome());
            processo.setDescricao(processoAtualizado.getDescricao());
            processo.setTipoProcesso(processoAtualizado.getTipoProcesso());
            return ResponseEntity.ok(processoRepository.save(processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id) {
        if (processoRepository.existsById(id)) {
            processoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
