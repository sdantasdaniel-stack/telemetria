package com.daniel.empresas.service;

import java.util.List;
import com.daniel.empresas.model.Empresa;
import com.daniel.empresas.repository.EmpresaRepository;

import com.daniel.empresas.exception.EmpresaNaoEncontradaException;
import com.daniel.empresas.exception.EstadoInvalidoException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.daniel.empresas.dto.request.UsuarioRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.dto.response.UsuarioComEmpresasIdDTO;
import com.daniel.empresas.dto.response.UsuarioResponseDTO;
import com.daniel.empresas.exception.EmailJaCadastradoException;
import com.daniel.empresas.exception.UsuarioNaoEncontradoException;
import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

//Recebem DTOs dos Controllers, aplicam regras de negócio, convertem para Entities, chamam os Repositories, e devolvem DTOs de resposta.
//Nunca devolvem Entities diretamente para o Controller.

// marca a classe para o Spring gerenciá-la e saber que essa classe tem lógica de negócio.
@Service

// para injeção das dependências
@RequiredArgsConstructor

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    
    private final EmpresaRepository empresaRepository;
    

    // PasswordEncoder é injetado aqui para criptografar a senha antes de salvar
    // o bean foi declarado no SecurityConfig
    private final PasswordEncoder passwordEncoder;

    // recebe o DTO com os dados do novo usuário enviados pelo Controller
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {

        // verifica se já existe um usuário com o mesmo email no banco
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new EmailJaCadastradoException("Já existe um usuário cadastrado com esse email");
        }

        // converte o DTO para Entity
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        // criptografa a senha antes de salvar
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(dto.role());
        usuario.setAtivo(true);

        // busca as empresas pelos IDs informados e vincula ao usuário
        // lança EmpresaNaoEncontradaException se algum ID não existir no banco
        List<Empresa> empresas = dto.empresasIds().stream()
                .map(id -> empresaRepository.findById(id)
                        .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id)))
                .toList();

        usuario.setEmpresas(empresas);

        // salva a Entity no banco
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getRole(),
                usuarioSalvo.isAtivo(),
                usuarioSalvo.getEmpresas().stream()
                        .map(e -> new EmpresaResponseDTO(e.getId(), e.getNome(), e.getCnpj(), e.getEmail(), e.isAtivo()))
                        .toList()
        );
    }

    public List<UsuarioComEmpresasIdDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> new UsuarioComEmpresasIdDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRole(),
                        usuario.isAtivo(),
                        usuario.getEmpresas().stream()
                                .map(Empresa::getId)
                                .toList()))
                .toList();
    }

    public UsuarioComEmpresasIdDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o id: " + id));
        return new UsuarioComEmpresasIdDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.isAtivo(),
                usuario.getEmpresas().stream()
                        .map(Empresa::getId)
                        .toList()
        );
    }

    // recebe o ID do usuário a atualizar e o DTO com os novos dados
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {

        // busca o usuário no banco — lança exceção se não encontrar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o id: " + id));

        // verifica se o novo email já pertence a outro usuário no banco
        // filter garante que não compara o usuário consigo mesmo
        usuarioRepository.findByEmail(dto.email())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new EmailJaCadastradoException("Já existe um usuário cadastrado com esse email"); });

        // atualiza os campos da Entity com os novos valores do DTO
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        // recriptografa a senha se ela foi alterada; se senha vier em branco, mantém a senha antiga
        if (dto.senha() != null && !dto.senha().isBlank()) {
                usuario.setSenha(passwordEncoder.encode(dto.senha()));
}
        usuario.setRole(dto.role());
        
        // atualiza as empresas do usuário
        List<Empresa> empresas = dto.empresasIds().stream()
                .map(empresaId -> empresaRepository.findById(empresaId)
                        .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + empresaId)))
                .collect(java.util.stream.Collectors.toList());
        usuario.setEmpresas(empresas);

        // salva a Entity atualizada no banco — o Hibernate executa o UPDATE
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        // converte a Entity atualizada para DTO e retorna para o Controller
        return new UsuarioResponseDTO(
                usuarioAtualizado.getId(),
                usuarioAtualizado.getNome(),
                usuarioAtualizado.getEmail(),
                usuarioAtualizado.getRole(),
                usuarioAtualizado.isAtivo(),
                usuarioAtualizado.getEmpresas().stream()
                        .map(e -> new EmpresaResponseDTO(e.getId(), e.getNome(), e.getCnpj(), e.getEmail(), e.isAtivo()))
                        .toList()
        );
    }

    
    
    // deleta o usuário permanentemente do banco
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o id: " + id));
        usuarioRepository.delete(usuario);
    }
    
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o id: " + id));
        if (!usuario.isAtivo()) {
            throw new EstadoInvalidoException("Usuário já está inativo");
        }
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void reativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o id: " + id));
        if (usuario.isAtivo()) {
            throw new EstadoInvalidoException("Usuário já está ativo");
        }
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
    
}