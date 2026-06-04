package com.daniel.empresas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.daniel.empresas.dto.request.DeviceRequestDTO;
import com.daniel.empresas.dto.response.DeviceResponseDTO;
import com.daniel.empresas.exception.DeviceNaoEncontradoException;
import com.daniel.empresas.exception.EmpresaNaoEncontradaException;
import com.daniel.empresas.exception.EstadoInvalidoException;
import com.daniel.empresas.exception.IdentificadorJaCadastradoException;
import com.daniel.empresas.model.Device;
import com.daniel.empresas.model.StatusEnum;
import com.daniel.empresas.repository.DeviceRepository;
import com.daniel.empresas.repository.EmpresaRepository;
import com.daniel.empresas.websocket.DeviceNotificador;

import lombok.RequiredArgsConstructor;

//marca a classe para o Spring gerenciá-la e saber que essa classe tem lógica de negócio.
@Service

// para injeção das dependências
@RequiredArgsConstructor

public class DeviceService {
	
	private final DeviceNotificador deviceNotificador;

    private final DeviceRepository deviceRepository;

    // EmpresaRepository é necessário para buscar a empresa pelo ID que vem no DTO
    private final EmpresaRepository empresaRepository;

    // converte Device para DTO — centralizado para não repetir em cada método
    private DeviceResponseDTO toDTO(Device device) {
        return new DeviceResponseDTO(
                device.getId(),
                device.getNome(),
                device.getIdentificador(),
                device.getStatus(),
                device.isAtivo(),
                device.getEmpresa() != null ? device.getEmpresa().getId() : null,
                device.getLatitude(),
                device.getLongitude()
        );
    }

    // lista devices de uma empresa específica (para página do role usuario)
    public List<DeviceResponseDTO> listarPorEmpresa(Long empresaId) {
        return deviceRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // recebe o DTO com os dados do novo device enviados pelo Controller
    public DeviceResponseDTO cadastrar(DeviceRequestDTO dto) {

        // verifica se já existe um device com o mesmo identificador no banco
        // se existir lança a exceção — o GlobalExceptionHandler retorna 409 para o cliente
        if (deviceRepository.findByIdentificador(dto.identificador()).isPresent()) {
            throw new IdentificadorJaCadastradoException("Já existe um device cadastrado com esse identificador");
        }

        // busca a empresa no banco pelo ID que veio no DTO
        // lança EmpresaNaoEncontradaException se não encontrar
        // o device precisa estar vinculado a uma empresa existente
        var empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + dto.empresaId()));
        
        if (!empresa.isAtivo()) {
            throw new EstadoInvalidoException("Não é possível cadastrar device em uma empresa inativa");
        }

        // converte o DTO para Entity
        Device device = new Device();
        device.setNome(dto.nome());
        device.setIdentificador(dto.identificador());

        // status nasce OFFLINE — o device só fica ONLINE quando se conectar ao sistema
        device.setStatus(StatusEnum.OFFLINE);

        // ativo = true porque um device recém cadastrado já nasce ativo
        device.setAtivo(true);

        // vincula o device à empresa encontrada
        device.setEmpresa(empresa);

        // latitude e longitude são opcionais — ficam nulos se não informados
        // devices sem coordenadas simplesmente não aparecem no mapa
        device.setLatitude(dto.latitude());
        device.setLongitude(dto.longitude());

        // salva a Entity no banco — o Hibernate executa o INSERT
        Device deviceSalvo = deviceRepository.save(device);

        // converte a Entity salva para DTO de resposta e retorna para o Controller
        return toDTO(deviceSalvo);
    }

    // recebe o ID do device que o cliente quer buscar
    public DeviceResponseDTO buscarPorId(Long id) {

        // busca o device no banco pelo ID
        // orElseThrow lança a exceção se o Optional estiver vazio
        // o GlobalExceptionHandler captura e retorna 404 para o cliente
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));

        // converte a Entity para DTO de resposta e retorna para o Controller
        return toDTO(device);
    }

    public List<DeviceResponseDTO> listarTodos() {

        // findAll() retorna todos os devices
        // stream() e map() convertem cada Entity para DTO
        return deviceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // recebe o ID do device a atualizar e o DTO com os novos dados
    public DeviceResponseDTO atualizar(Long id, DeviceRequestDTO dto) {

        // busca o device no banco — lança exceção se não encontrar
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));

        // verifica se o novo identificador já pertence a outro device no banco
        // filter garante que não compara o device consigo mesmo
        deviceRepository.findByIdentificador(dto.identificador())
                .filter(d -> !d.getId().equals(id))
                .ifPresent(d -> { throw new IdentificadorJaCadastradoException("Já existe um device cadastrado com esse identificador"); });

        // busca a nova empresa se o empresaId foi alterado
        var empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada com o id: " + dto.empresaId()));

        // atualiza os campos da Entity com os novos valores do DTO
        device.setNome(dto.nome());
        device.setIdentificador(dto.identificador());
        device.setEmpresa(empresa);

        // atualiza as coordenadas — aceita nulo se o admin deixar em branco
        device.setLatitude(dto.latitude());
        device.setLongitude(dto.longitude());

        // salva a Entity atualizada no banco — o Hibernate executa o UPDATE
        Device deviceAtualizado = deviceRepository.save(device);

        // converte a Entity atualizada para DTO e retorna para o Controller
        return toDTO(deviceAtualizado);
    }

    

    // método especial usado pelo WebSocket para atualizar o status online/offline do device em tempo real
    // não precisa de DTO — só recebe o ID e o novo status
    public void atualizarStatus(Long id, StatusEnum status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));
        device.setStatus(status);
        deviceRepository.save(device);
        // notifica todos os browsers conectados sobre a mudança de status
        deviceNotificador.notificar(id, status);
    }

    // versão filtrada para o role USER — retorna apenas devices ativos da empresa
    // o ADMIN continua usando listarPorEmpresa() que retorna todos sem filtro, para que ele possa reativar devices
    public List<DeviceResponseDTO> listarAtivosPorEmpresa(Long empresaId) {
        return deviceRepository.findByEmpresaIdAndAtivoTrue(empresaId)
                .stream()
                // "?" - Usado para substituir estruturas simples de if / else em uma única linha. Ele avalia uma condição e
                // retorna um de dois valores possíveis. Usado para evitar NullPointerException se retornar nulo
                .map(this::toDTO)
                .toList();
    }

    // retorna apenas devices ativos com empresa ativa e que já têm coordenadas
    // devices sem latitude/longitude são ignorados — não aparecem no mapa
    public List<DeviceResponseDTO> listarParaMapa() {
        return deviceRepository.findDispositivosParaMapa()
                .stream()
                .map(this::toDTO)
                .toList();
    }
    
    // busca apenas devices ativos — usado pelo endpoint da API para role USER
    // usuário normal não deve ver devices inativos
    public List<DeviceResponseDTO> listarAtivos() {
    	return deviceRepository.findAll()
    			.stream()
    			.filter(Device::isAtivo)
    			.map(this::toDTO)
    			.toList();
    }

    // deleta o device permanentemente do banco
    public void deletar(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));
        deviceRepository.delete(device);
        deviceNotificador.notificarReload();
    }

    public void desativar(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));
        if (!device.isAtivo()) {
            throw new EstadoInvalidoException("Device já está inativo");
        }
        device.setAtivo(false);
        deviceRepository.save(device);
        System.out.println(">>> desativar: chamando notificarReload");
        deviceNotificador.notificarReload();
        System.out.println(">>> desativar: notificarReload retornou");
    }

    public void reativar(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNaoEncontradoException("Device não encontrado com o id: " + id));
        if (device.isAtivo()) {
            throw new EstadoInvalidoException("Device já está ativo");
        }
        device.setAtivo(true);
        deviceRepository.save(device);
        deviceNotificador.notificarReload();
    }
}