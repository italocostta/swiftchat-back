package com.pd.swiftchat.controller;

import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.service.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setores")
public class SetorController {

    @Autowired
    private SetorService setorService;

    @GetMapping
    public List<Setor> listarSetores() {
        return setorService.getAllSetores();
    }

    @PostMapping
    public Setor criarSetor(@RequestBody Setor setor) {
        return setorService.createSetor(setor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Setor> obterSetor(@PathVariable Long id) {
        return setorService.getSetorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Setor> atualizarSetor(@PathVariable Long id, @RequestBody Setor setorAtualizado) {
        Setor setor = setorService.updateSetor(id, setorAtualizado);
        if (setor != null) {
            return ResponseEntity.ok(setor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSetor(@PathVariable Long id) {
        setorService.deleteSetor(id);
        return ResponseEntity.noContent().build();
    }
}
