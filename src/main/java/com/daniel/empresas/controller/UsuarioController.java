package com.daniel.empresas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.daniel.empresas.dto.request.UsuarioRequestDTO;
import com.daniel.empresas.dto.response.UsuarioResponseDTO;
import com.daniel.empresas.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.daniel.empresas.dto.response.UsuarioComEmpresasIdDTO;
// marca a classe como Controller REST — recebe requisições HTTP e retorna JSON
@RestController

// Essa anotação mapeia URLs de requisições HTTP para classes ou métodos específicos do seu controlador
// prefixo de todas as rotas desse Controller — toda rota começa com /usuarios
@RequestMapping("/usuarios")

// gera construtor com todos os campos final para injeção pelo Spring
@RequiredArgsConstructor

public class UsuarioController {

    // único campo injetado — o Controller só conhece o Service
    private final UsuarioService usuarioService;

    // Essa anotação do Spring é usada para mapear requisições HTTP GET para métodos específicos em controladores
    // responde GET /usuarios — retorna todos os usuários — apenas ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioComEmpresasIdDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioComEmpresasIdDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // @PostMapping do Spring é usada para mapear requisições HTTP POST em métodos de controladores REST
    // responde POST /usuarios — cadastra um novo usuário — apenas ADMIN
    // @Valid ativa o Bean Validation nos campos do DTO
    // @RequestBody converte o JSON do corpo da requisição para UsuarioRequestDTO
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioSalvo = usuarioService.cadastrar(dto);
        // retorna o usuário salvo com status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

    // responde PUT /usuarios/{id} — atualiza um usuário existente — apenas ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    // responde DELETE /usuarios/{id} — desativa um usuário — apenas ADMIN
    // retorna Void porque não há dado para devolver
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        // O método .build() é necessário no DELETE porque você está retornando uma resposta sem corpo (Void)
        // retorna status 204 No Content — deu certo mas não há corpo na resposta
        return ResponseEntity.noContent().build();
    }

    // responde DELETE /usuarios/{id}/deletar — deleta permanentemente um usuário — apenas ADMIN
    @DeleteMapping("/{id}/deletar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // responde PATCH /usuarios/{id}/reativar — reativa um usuário desativado — apenas ADMIN
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        usuarioService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}