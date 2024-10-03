package com.pd.swiftchat.controller;

import com.pd.swiftchat.dto.ProcessoDTO;
import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.*;
import com.pd.swiftchat.repository.UsuarioRepository;
import com.pd.swiftchat.service.ProcessoService;
import com.pd.swiftchat.service.SetorService;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/processos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @Autowired
    private SetorService setorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Endpoint para avaliar o processo (Deferido ou Indeferido)
    @Secured("FUNCIONARIO")
    @PutMapping("/{id}/avaliar")
    public ResponseEntity<Processo> avaliarProcesso(
            @PathVariable Long id,
            @RequestParam String statusProcesso,
            @RequestParam(required = false) String observacao) {
        Processo processoAtualizado = processoService.avaliarProcesso(id, statusProcesso, observacao);
        return ResponseEntity.ok(processoAtualizado);
    }

    @Secured("FUNCIONARIO")
    @PutMapping("/{id}/status")
    public ResponseEntity<Processo> atualizarStatusProcesso(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {

        Optional<Processo> processoOptional = processoService.getProcessoById(id);

        if (processoOptional.isPresent()) {
            Processo processo = processoOptional.get();
            String status = statusData.get("statusProcesso");
            String observacao = statusData.get("observacao");

            processo.setStatusProcesso(status);
            processo.setObservacao(observacao);

            Processo processoAtualizado = processoService.updateProcesso(id, processo);
            return ResponseEntity.ok(processoAtualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping("/{id}")
    public ResponseEntity<ProcessoDTO> obterProcessoPorId(@PathVariable Long id) {
        return processoService.getProcessoById(id)
                .map(processo -> ResponseEntity.ok(convertToDTO(processo)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping
    public ResponseEntity<List<ProcessoDTO>> listarProcessos(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuarioLogado = getUsuarioByUsername(userDetails.getUsername());
        List<Processo> processos = processoService.getAllProcessosByRole(usuarioLogado);

        List<ProcessoDTO> processosDTO = processos.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(processosDTO);
    }

    @Secured("USUARIO")
    @PostMapping
    public ResponseEntity<String> criarProcesso(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("processo") Processo processo,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos
    ) throws IOException {
        if (arquivos == null || arquivos.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum arquivo foi enviado."); // Rejeita o processo se não houver arquivos
        }

        // Obtém o usuário autenticado
        String cpfOuCnpj = userDetails.getUsername(); // Captura o CPF ou CNPJ do usuário logado
        Usuario usuarioLogado = getUsuarioByUsername(cpfOuCnpj); // Busca o usuário no banco de dados

        // Associa o CPF ou CNPJ ao processo
        if (cpfOuCnpj.length() == 11) {
            processo.setCpf(cpfOuCnpj); // Se for CPF
        } else {
            processo.setCnpj(cpfOuCnpj); // Se for CNPJ
        }

        // Associa o usuário ao processo
        processo.setUsuario(String.valueOf(usuarioLogado)); // Define o usuário no processo

        // Salvando arquivos no diretório do usuário
        for (MultipartFile arquivo : arquivos) {
            try {
                String nomeArquivo = salvarArquivo(arquivo, cpfOuCnpj); // Passa o arquivo e o CPF/CNPJ
                processo.getArquivos().add(nomeArquivo);  // Adiciona o arquivo à lista de arquivos do processo
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("Arquivo duplicado: " + arquivo.getOriginalFilename());
            }
        }

        Processo novoProcesso = processoService.createProcesso(processo);
        return ResponseEntity.ok("Processo criado com sucesso!");
    }







    @Secured("FUNCIONARIO")
    @PutMapping("/{id}/setor/{setorId}")
    public ResponseEntity<Processo> moverProcessoParaSetor(@PathVariable Long id, @PathVariable Long setorId) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        Optional<Setor> setorDestino = setorService.getSetorById(setorId);
        if (processoExistente.isPresent() && setorDestino.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setSetor(setorDestino.get());
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("FUNCIONARIO")
    @PutMapping("/{id}/encaminhar")
    public ResponseEntity<Processo> encaminharParaSetorEspecifico(@PathVariable Long id) {
        return processoService.getProcessoById(id)
                .map(processo -> {
                    Setor setorEspecifico = setorService.getSetorPorTipoProcesso(processo.getTipoProcesso());
                    processo.setSetor(setorEspecifico);
                    return ResponseEntity.ok(processoService.updateProcesso(id, processo));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Secured("FUNCIONARIO")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarProcesso(@PathVariable Long id) {
        return processoService.getProcessoById(id)
                .map(processo -> {
                    processoService.deleteProcesso(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadArquivos(@PathVariable Long id) throws IOException {
        Optional<Processo> processoOpt = processoService.getProcessoById(id);
        if (!processoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Processo processo = processoOpt.get();
        List<String> arquivos = processo.getArquivos();

        // Criar um arquivo ZIP temporário
        Path zipPath = Files.createTempFile("arquivos_processo_" + processo.getNumeroProcesso(), ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (String arquivo : arquivos) {
                Path arquivoPath = Paths.get("uploads").resolve(arquivo).toAbsolutePath().normalize();
                zipOutputStream.putNextEntry(new ZipEntry(arquivo));
                Files.copy(arquivoPath, zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }

        Resource resource = new UrlResource(zipPath.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arquivos_processo_" + processo.getNumeroProcesso() + ".zip\"")
                    .body(resource);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping("/{id}/download-todos-arquivos")
    public ResponseEntity<Resource> downloadTodosArquivos(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Processo> processoOpt = processoService.getProcessoById(id);

        if (processoOpt.isPresent()) {
            Processo processo = processoOpt.get();

            // Garantir que os arquivos sejam únicos
            Set<String> arquivos = new HashSet<>(processo.getArquivos());

            String cpfOuCnpj = processo.getCpf() != null ? processo.getCpf() : processo.getCnpj(); // Obter CPF ou CNPJ do processo

            if (arquivos == null || arquivos.isEmpty()) {
                return ResponseEntity.badRequest().body(null); // Verifique se há arquivos anexados
            }

            try {
                // Caminho temporário para o arquivo ZIP
                Path zipPath = Files.createTempFile("arquivos_processo_" + processo.getId(), ".zip");
                System.out.println("Criando ZIP em: " + zipPath.toString()); // Log de depuração

                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                    for (String arquivo : arquivos) {
                        // Caminho completo para os arquivos, usando CPF ou CNPJ
                        Path filePath = Paths.get("uploads/usuarios").resolve(cpfOuCnpj).resolve(arquivo).toAbsolutePath().normalize();
                        System.out.println("Adicionando arquivo ao ZIP: " + filePath.toString()); // Log de depuração

                        if (Files.exists(filePath)) {
                            zos.putNextEntry(new ZipEntry(arquivo));
                            Files.copy(filePath, zos);
                            zos.closeEntry();
                        } else {
                            System.out.println("Arquivo não encontrado: " + filePath.toString()); // Log de depuração
                        }
                    }
                }

                // Retorna o arquivo ZIP como um recurso
                Resource resource = new UrlResource(zipPath.toUri());
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"arquivos_processo_" + processo.getId() + ".zip\"")
                        .body(resource);
            } catch (IOException e) {
                e.printStackTrace(); // Adicione o stack trace para capturar o erro
                throw new RuntimeException("Erro ao criar o arquivo ZIP", e);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }







    private String salvarArquivoParaUsuario(MultipartFile arquivo, Usuario usuario) throws IOException {
        String diretorioUsuario = "uploads/" + (usuario.getCpf() != null ? usuario.getCpf() : usuario.getCnpj());

        Path caminhoDiretorioUsuario = Paths.get(diretorioUsuario);
        if (!Files.exists(caminhoDiretorioUsuario)) {
            Files.createDirectories(caminhoDiretorioUsuario);
        }

        Path caminhoArquivo = caminhoDiretorioUsuario.resolve(Objects.requireNonNull(arquivo.getOriginalFilename()));
        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        return caminhoArquivo.getFileName().toString();
    }





    @Secured("USUARIO")
    @PutMapping("/{id}/anexar")
    public ResponseEntity<String> anexarArquivos(
            @PathVariable Long id,
            @RequestPart(value = "novosArquivos", required = false) List<MultipartFile> novosArquivos,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        Optional<Processo> processoOpt = processoService.getProcessoById(id);

        if (!processoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Processo processo = processoOpt.get();

        // Obter o usuário autenticado
        String cpfOuCnpj = userDetails.getUsername(); // Captura o CPF ou CNPJ do usuário logado

        // Verifica se novos arquivos foram anexados
        if (novosArquivos != null && !novosArquivos.isEmpty()) {
            for (MultipartFile arquivo : novosArquivos) {
                try {
                    // Passa o arquivo e o CPF ou CNPJ do usuário logado
                    String nomeArquivo = salvarArquivo(arquivo, cpfOuCnpj); // Passa os dois argumentos
                    processo.getArquivos().add(nomeArquivo);  // Adiciona os novos arquivos à lista de arquivos do processo
                } catch (RuntimeException e) {
                    return ResponseEntity.badRequest().body("Arquivo duplicado: " + arquivo.getOriginalFilename());
                }
            }
        }

        // Salva o processo atualizado com os novos arquivos anexados
        Processo processoAtualizado = processoService.updateProcesso(id, processo);
        return ResponseEntity.ok("Arquivos anexados com sucesso!");
    }








    // Métodos auxiliares para evitar repetição
    private String salvarArquivo(MultipartFile arquivo, String cpfOuCnpj) throws IOException {
        // Definir o diretório com base no CPF ou CNPJ do usuário
        String diretorioUploads = "uploads/usuarios/" + cpfOuCnpj;

        // Criar o diretório se ele não existir
        Path caminhoDiretorioUploads = Paths.get(diretorioUploads);
        if (!Files.exists(caminhoDiretorioUploads)) {
            Files.createDirectories(caminhoDiretorioUploads);
        }

        // Caminho completo do arquivo
        Path caminhoArquivo = caminhoDiretorioUploads.resolve(Objects.requireNonNull(arquivo.getOriginalFilename()));

        // Copiar o arquivo para o diretório
        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        // Retorna o nome do arquivo
        return arquivo.getOriginalFilename();
    }








    private Usuario getUsuarioByUsername(String username) {
        return username.length() == 11
                ? usuarioRepository.findByCpf(username).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"))
                : usuarioRepository.findByCnpj(username).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Setor getSetorIntermediario() {
        return setorService.getSetorByNome("Setor Intermediario")
                .orElseThrow(() -> new ResourceNotFoundException("Setor Intermediário não encontrado"));
    }

    private ProcessoDTO convertToDTO(Processo processo) {
        ProcessoDTO dto = new ProcessoDTO();
        dto.setId(processo.getId());
        dto.setNome(processo.getNome());
        dto.setDescricao(processo.getDescricao());
        dto.setNumeroProcesso(processo.getNumeroProcesso());
        dto.setCpf(processo.getCpf());
        dto.setCnpj(processo.getCnpj());
        dto.setTipoPessoa(processo.getTipoPessoa());
        dto.setTipoProcesso(processo.getTipoProcesso());
        dto.setSetor(processo.getSetor());
        dto.setUsuarioNome(processo.getUsuario());
        dto.setArquivo(processo.getArquivo());
        dto.setStatusProcesso(processo.getStatusProcesso());
        dto.setObservacao(processo.getObservacao());
        return dto;
    }

    private ProcessoDTO mapearParaDTO(Processo processo) {
        ProcessoDTO dto = new ProcessoDTO();
        dto.setId(processo.getId());
        dto.setNome(processo.getNome());
        dto.setDescricao(processo.getDescricao());
        dto.setNumeroProcesso(processo.getNumeroProcesso());
        dto.setCpf(processo.getCpf());
        dto.setCnpj(processo.getCnpj());
        dto.setTipoPessoa(processo.getTipoPessoa());
        dto.setTipoProcesso(processo.getTipoProcesso());
        dto.setSetor(processo.getSetor());
        dto.setUsuarioNome(processo.getUsuario());
        dto.setArquivos(processo.getArquivos());
        dto.setStatusProcesso(processo.getStatusProcesso());
        dto.setObservacao(processo.getObservacao());
        return dto;
    }
}
