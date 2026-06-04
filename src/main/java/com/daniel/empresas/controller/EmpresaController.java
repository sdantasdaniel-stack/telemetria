package com.daniel.empresas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.service.EmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// marca a classe como Controller REST — recebe requisições HTTP e retorna JSON
@RestController

// Essa anotação mapeia URLs de requisições HTTP para classes ou métodos específicos do seu controlador
// prefixo de todas as rotas desse Controller — toda rota começa com /empresas
@RequestMapping("/empresas")

// gera construtor com todos os campos final para injeção pelo Spring
@RequiredArgsConstructor

public class EmpresaController {

    // único campo injetado — o Controller só conhece o Service
    private final EmpresaService empresaService;

    // injetado apenas para o teste da Hipótese A — logar o hash e comparar com o do Notificador e do Bean JSF
    private final SimpMessagingTemplate messagingTemplate;

    // A anotação @GetMapping do Spring Boot serve para mapear requisições HTTP do tipo GET para um método específico do seu controlador
    // Ou seja, se o usuario quiser fazer algo que envolva um GET, O SPRING SÓ VAI PROCURAR MÉTODOS MARCADOS COM ESSA ANOTAÇÃO
    // responde GET /empresas — retorna todas as empresas — apenas ADMIN pode listar todas (ativas e inativas)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmpresaResponseDTO>> listarTodas() {
        // chama o Service que busca e converte todas as empresas para DTO
        List<EmpresaResponseDTO> empresas = empresaService.listarTodas();
        // retorna a lista com status 200 OK
        return ResponseEntity.ok(empresas);
    }

    // responde GET /empresas/ativas — retorna apenas empresas ativas — apenas USER pode acessar
    // USER não deve ver empresas inativas
    @GetMapping("/ativas")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EmpresaResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(empresaService.listarAtivas());
    }

    // responde GET /empresas/{id} — retorna uma empresa pelo ID — apenas ADMIN
    // @PathVariable extrai o valor {id} da URL e passa para o parâmetro id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponseDTO> buscarPorId(@PathVariable Long id) {
        // chama o Service — lança EmpresaNaoEncontradaException se não existir
        EmpresaResponseDTO empresa = empresaService.buscarPorId(id);
        // retorna a empresa com status 200 OK
        return ResponseEntity.ok(empresa);
    }

    // @PostMapping do Spring é usada para mapear requisições HTTP POST em métodos de controladores REST
    // responde POST /empresas — cadastra uma nova empresa — apenas ADMIN
    // @Valid ativa o Bean Validation — valida os campos do DTO antes de chegar no Service
    // @RequestBody converte o JSON do corpo da requisição para EmpresaRequestDTO
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponseDTO> cadastrar(@RequestBody @Valid EmpresaRequestDTO dto) {
        // chama o Service que valida e salva a empresa
        EmpresaResponseDTO empresaSalva = empresaService.cadastrar(dto);
        // retorna a empresa salva com status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaSalva);
    }

    // responde PUT /empresas/{id} — atualiza uma empresa existente — apenas ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid EmpresaRequestDTO dto) {
        // chama o Service que valida e atualiza a empresa
        EmpresaResponseDTO empresaAtualizada = empresaService.atualizar(id, dto);
        // retorna a empresa atualizada com status 200 OK
        return ResponseEntity.ok(empresaAtualizada);
    }

    // responde DELETE /empresas/{id} — desativa uma empresa — apenas ADMIN
    // retorna Void porque não há dado para devolver — só confirmação de que foi feito
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        // loga o hash do template injetado no Controller — comparar com o hash do Notificador e do Bean JSF
        System.out.println(">>> REST Controller template hash: " + System.identityHashCode(messagingTemplate));
        // chama o Service que desativa a empresa — seta ativo = false
        empresaService.desativar(id);
        // retorna status 204 No Content — deu certo mas não há corpo na resposta
        // O método .build() é necessário no DELETE porque você está retornando uma resposta sem corpo (Void)
        return ResponseEntity.noContent().build();
    }

    // responde DELETE /empresas/{id}/deletar — deleta permanentemente uma empresa — apenas ADMIN
    // URL diferente do /empresas/{id} que apenas desativa
    @DeleteMapping("/{id}/deletar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // chama o Service que deleta permanentemente a empresa do banco
        empresaService.deletar(id);
        // retorna 204 No Content — operação realizada sem corpo de resposta
        return ResponseEntity.noContent().build();
    }

    // responde PATCH /empresas/{id}/reativar — reativa uma empresa desativada — apenas ADMIN
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        empresaService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}