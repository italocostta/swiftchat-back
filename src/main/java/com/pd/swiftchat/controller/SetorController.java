package com.pd.swiftchat.controller;

import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.service.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
public class SetorController {

    @Autowired
    private SetorService setorService;

    @GetMapping
    public List<Setor> listarSetores() {
        return setorService.getAllSetores();
    }

    @Secured("FUNCIONARIO")  // Permitir apenas que funcionários criem setores
    @PostMapping
    public ResponseEntity<?> criarSetor(@RequestBody Setor setor) {
        try {
            Setor novoSetor = setorService.createSetor(setor);
            return ResponseEntity.ok(novoSetor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Setor> obterSetor(@PathVariable Long id) {
        return setorService.getSetorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Secured("FUNCIONARIO")  // Permitir apenas que funcionários atualizem setores
    @PutMapping("/{id}")
    public ResponseEntity<Setor> atualizarSetor(@PathVariable Long id, @RequestBody Setor setorAtualizado) {
        Setor setor = setorService.updateSetor(id, setorAtualizado);
        if (setor != null) {
            return ResponseEntity.ok(setor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("FUNCIONARIO")  // Permitir apenas que funcionários deletem setores
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSetor(@PathVariable Long id) {
        setorService.deleteSetor(id);
        return ResponseEntity.noContent().build();
    }
}
