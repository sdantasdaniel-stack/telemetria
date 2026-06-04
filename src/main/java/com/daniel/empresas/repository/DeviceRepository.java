package com.daniel.empresas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.daniel.empresas.model.Device;


//Marca a interface para o Spring saber que ela é um Repository e gerenciá-la.
@Repository

//estende JPAr. para poder herdar e conseq. usar metodos e atributos desse repositorio

//recebe o tipo da entity e o tipo do ID dessa entity

//Quando você estende o JpaRepository, os métodos básicos já aparecem automaticamente — salvar, buscar por ID, listar todos, 
//deletar. Você não precisa escrever nenhum deles.

// Porque INTERFACE e não CLASSE:
// Se fosse uma classe, você teria que implementar manualmente todos os métodos do JpaRepository, como save, findById, findAll, delete, etc. Com
// a interface, o Spring lê o contrato e gera tudo sozinho.
// Os métodos customizados como findByIdentificador e findByEmpresaId também funcionam por isso — o Spring lê o nome do método, interpreta como
// uma query e gera o SQL automaticamente. Em uma classe isso não aconteceria.

public interface DeviceRepository extends JpaRepository<Device, Long>{
	
	// usa o optional porque retorna nulo se nao encontrar nada
	// metodo findBy veio do JpaRepository
	// serve para o service usar para evitar duplicidade
	Optional<Device> findByIdentificador(String identificador);
	
	// busca devices pelo ID da empresa (adicionado para usar nas páginas do role usuario)
	List<Device> findByEmpresaId(Long empresaId);
	
	// deleta todos os devices de uma empresa pelo ID da empresa
	// usado antes de deletar a empresa para evitar violação de FK
	void deleteByEmpresaId(Long empresaId);
	
	// busca apenas devices ativos de uma empresa
	// o Spring Data interpreta o nome do método e gera o SQL automaticamente:
	// SELECT * FROM devices WHERE empresa_id = ? AND ativo = true
	List<Device> findByEmpresaIdAndAtivoTrue(Long empresaId);
	
	// busca devices ativos cujas empresas também estão ativas e que já têm coordenadas salvas
    // usado exclusivamente pela página do mapa
    @Query("SELECT d FROM Device d WHERE d.ativo = true AND d.empresa.ativo = true AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
    List<Device> findDispositivosParaMapa();

}

