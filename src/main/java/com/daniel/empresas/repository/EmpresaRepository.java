package com.daniel.empresas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daniel.empresas.model.Empresa;

// Marca a interface para o Spring saber que ela é um Repository e gerenciá-la.
@Repository

//estende JPAr. para poder herdar e conseq. usar metodos e atributos desse repositorio
//recebe o tipo da entity e o tipo do ID dessa entity

//Quando você estende o JpaRepository, os métodos básicos já aparecem automaticamente — salvar, buscar por ID, listar todos, 
//deletar. Você não precisa escrever nenhum deles.

//Porque INTERFACE e não CLASSE:
//Se fosse uma classe, você teria que implementar manualmente todos os métodos do JpaRepository, como save, findById, findAll, delete, etc. Com
//a interface, o Spring lê o contrato e gera tudo sozinho.
//Os métodos customizados como findByIdentificador e findByEmpresaId também funcionam por isso — o Spring lê o nome do método, interpreta como
//uma query e gera o SQL automaticamente. Em uma classe isso não aconteceria.

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
	
	// usa o optional porque retorna nulo se nao encontrar nada
	// metodo findBy veio do JpaRepository
	// serve para o service usar para evitar duplicidade
	Optional<Empresa> findByCnpj(String cnpj);
	
	// usa o optional porque retorna nulo se nao encontrar nada
	// metodo findBy veio do JpaRepository
	// serve para o service usar para evitar duplicidade
	Optional<Empresa> findByEmail(String email);

}
