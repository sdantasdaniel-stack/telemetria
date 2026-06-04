package com.daniel.empresas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daniel.empresas.dto.request.EmpresaRequestDTO;
import com.daniel.empresas.dto.response.EmpresaResponseDTO;
import com.daniel.empresas.exception.CnpjJaCadastradoException;
import com.daniel.empresas.exception.EmailJaCadastradoException;
import com.daniel.empresas.exception.EmpresaNaoEncontradaException;
import com.daniel.empresas.exception.EstadoInvalidoException;
import com.daniel.empresas.model.Empresa;
import com.daniel.empresas.model.Usuario;
import com.daniel.empresas.repository.DeviceRepository;
import com.daniel.empresas.repository.EmpresaRepository;
import com.daniel.empresas.repository.UsuarioRepository;
import com.daniel.empresas.websocket.EmpresaNotificador;

import lombok.RequiredArgsConstructor;

//Recebem DTOs dos Controllers, aplicam regras de negócio, convertem para Entities, chamam os Repositories, e devolvem DTOs de resposta.
//Nunca devolvem Entities diretamente para o Controller.

//marca a classe para o Spring gerenciá-la e saber que essa classe tem lógica de negócio.
@Service

// para injeção das dependências
@RequiredArgsConstructor

public class EmpresaService {

    private final UsuarioRepository usuarioRepository;

    private final DeviceRepository deviceRepository;

    private final EmpresaRepository empresaRepository;

    private final EmpresaNotificador empresaNotificador;

    // recebe o DTO com os dados da nova empresa enviados pelo Controller
    public EmpresaResponseDTO cadastrar(EmpresaRequestDTO dto) {

        // verifica se já existe uma empresa com o mesmo CNPJ no banco
        // se existir lança a exceção — o GlobalExceptionHandler retorna 409 para o cliente
        if (empresaRepository.findByCnpj(dto.cnpj()).isPresent()) {
            throw new CnpjJaCadastradoException("Já existe uma empresa cadastrada com esse CNPJ");
        }

        // verifica se já existe uma empresa com o mesmo email no banco
        // se existir lança a exceção — o GlobalExceptionHandler retorna 409 para o cliente
        if (empresaRepository.findByEmail(dto.email()).isPresent()) {
            throw new EmailJaCadastradoException("Já existe uma empresa cadastrada com esse email");
        }

        // converte o DTO para Entity
        // ativo = true porque uma empresa recém cadastrada já nasce ativa
        Empresa empresa = new Empresa();
        empresa.setNome(dto.nome());
        empresa.setCnpj(dto.cnpj());
        empresa.setEmail(dto.email());
        empresa.setAtivo(true);

        // salva a Entity no banco — o Hibernate executa o INSERT
        // o objeto retornado já tem o ID gerado pelo banco
        Empresa empresaSalva = empresaRepository.save(empresa);

        // converte a Entity salva para DTO de resposta e retorna para o Controller
        // nunca retorna a Entity diretamente — o DTO controla o que o cliente pode ver
        return new EmpresaResponseDTO(
                empresaSalva.getId(),
                empresaSalva.getNome(),
                empresaSalva.getCnpj(),
                empresaSalva.getEmail(),
                empresaSalva.isAtivo()
        );
    }

    // recebe o ID da empresa que o cliente quer buscar
    public EmpresaResponseDTO buscarPorId(Long id) {

        // busca a empresa no banco pelo ID
        // orElseThrow lança a exceção se o Optional estiver vazio — ou seja, se não existir empresa com esse ID
        // o GlobalExceptionHandler captura a exceção e retorna 404 para o cliente
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id));

        // converte a Entity para DTO de resposta e retorna para o Controller
        // nunca retorna a Entity diretamente — o DTO controla o que o cliente pode ver
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getCnpj(),
                empresa.getEmail(),
                empresa.isAtivo()
        );
    }

    // busca todas as empresas no banco e converte cada uma para DTO de resposta
    public List<EmpresaResponseDTO> listarTodas() {

        // findAll() é um método herdado do JpaRepository — retorna uma lista com todas as empresas
        // stream() transforma a lista em um fluxo para processar cada elemento. Usa stream para poder usar o map()
        // map() converte cada Entity Empresa para EmpresaResponseDTO (map() serve para converter de um tipo para outro)
        // toList() coleta os elementos processados de volta para uma lista
        return empresaRepository.findAll()
                .stream()
                .map(empresa -> new EmpresaResponseDTO(
                        empresa.getId(),
                        empresa.getNome(),
                        empresa.getCnpj(),
                        empresa.getEmail(),
                        empresa.isAtivo()))
                .toList();
    }

    // recebe o ID da empresa a atualizar e o DTO com os novos dados
    public EmpresaResponseDTO atualizar(Long id, EmpresaRequestDTO dto) {

        // busca a empresa no banco — lança exceção se não encontrar
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id));

        // Busca no banco se já existe alguma empresa com o CNPJ que chegou no DTO
        // findByCnpj retorna Optional<Empresa> — pode ter uma empresa ou estar vazio
        empresaRepository.findByCnpj(dto.cnpj())

             // filter para que passe apenas as empresas com id diferente do id solicitado no método (para que nao compare com a própria empresa) (! é operador de negação)
             .filter(e -> !e.getId().equals(id))

             // se tiver alguma com cnpj igual sem ser a própria empresa escolhida, o ifPresent manda para o throw new que lança a exceção
             // o GlobalExceptionHandler captura e retorna 409 para o cliente
             .ifPresent(e -> { throw new CnpjJaCadastradoException("Já existe uma empresa cadastrada com esse CNPJ"); });

        // verifica se o novo email já pertence a outra empresa no banco
        empresaRepository.findByEmail(dto.email())
                .filter(e -> !e.getId().equals(id))
                .ifPresent(e -> { throw new EmailJaCadastradoException("Já existe uma empresa cadastrada com esse email"); });

        // atualiza os campos da Entity com os novos valores do DTO
        empresa.setNome(dto.nome());
        empresa.setCnpj(dto.cnpj());
        empresa.setEmail(dto.email());

        // salva a Entity atualizada no banco — o Hibernate executa o UPDATE
        Empresa empresaAtualizada = empresaRepository.save(empresa);

        // converte a Entity atualizada para DTO e retorna para o Controller
        return new EmpresaResponseDTO(
                empresaAtualizada.getId(),
                empresaAtualizada.getNome(),
                empresaAtualizada.getCnpj(),
                empresaAtualizada.getEmail(),
                empresaAtualizada.isAtivo()
        );
    }

    

    // deleta a empresa permanentemente do banco
    // lança exceção se não encontrar — o GlobalExceptionHandler retorna 404
    // @Transactional garante que tudo dentro do método acontece em uma única transação de banco. Se qualquer passo falhar, tudo
    // é revertido automaticamente — nenhuma exclusão parcial fica no banco.
    // notifica os clientes conectados via WebSocket para que a página do usuário atualize em tempo real
    @Transactional
    public void deletar(Long id) {

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id));

        // busca todos os usuários que têm essa empresa vinculada
        // e remove a empresa da lista de cada um — isso gera os DELETEs na tabela usuario_empresa
        // é feito pelo lado dono (Usuario) porque o JPA só persiste mudanças feitas no lado dono do ManyToMany
        List<Usuario> usuariosVinculados = empresa.getUsuarios();
        if (usuariosVinculados != null) {
            for (Usuario usuario : usuariosVinculados) {
                usuario.getEmpresas().remove(empresa);
                usuarioRepository.save(usuario);
            }
        }

        // deleta todos os devices que pertencem a essa empresa
        deviceRepository.deleteByEmpresaId(id);

        // agora a empresa pode ser deletada sem violar nenhuma FK
        empresaRepository.delete(empresa);

        // notifica os clientes conectados via WebSocket — caminho direto para teste da Hipótese A
        empresaNotificador.notificar(id, "deletada");
    }

    public void desativar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id));
        if (!empresa.isAtivo()) {
            throw new EstadoInvalidoException("Empresa já está inativa");
        }
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
        empresaNotificador.notificar(id, "desativada");
    }

    public void reativar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + id));
        if (empresa.isAtivo()) {
            throw new EstadoInvalidoException("Empresa já está ativa");
        }
        empresa.setAtivo(true);
        empresaRepository.save(empresa);
        empresaNotificador.notificar(id, "reativada");
    }

    // busca apenas empresas ativas — usado pelo endpoint da API para role USER
    // usuário normal não deve ver empresas inativas
    public List<EmpresaResponseDTO> listarAtivas() {
        return empresaRepository.findAll()
                .stream()
                .filter(Empresa::isAtivo)
                .map(empresa -> new EmpresaResponseDTO(
                        empresa.getId(),
                        empresa.getNome(),
                        empresa.getCnpj(),
                        empresa.getEmail(),
                        empresa.isAtivo()))
                .toList();
    }
}

