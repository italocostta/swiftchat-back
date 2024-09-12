package com.pd.swiftchat.controller;

import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tiposprocessos")
public class TipoProcessoController {

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @GetMapping
    public List<TipoProcesso> listarTiposProcessos() {
        return tipoProcessoRepository.findAll();
    }

    @PostMapping
    public TipoProcesso criarTipoProcesso(@RequestBody TipoProcesso tipoProcesso) {
        return tipoProcessoRepository.save(tipoProcesso);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoProcesso> obterTipoProcesso(@PathVariable Long id) {
        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(id);
        return tipoProcesso.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTipoProcesso(@PathVariable Long id) {
        if (tipoProcessoRepository.existsById(id)) {
            tipoProcessoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
