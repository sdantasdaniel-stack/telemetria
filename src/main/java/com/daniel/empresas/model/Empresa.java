package com.daniel.empresas.model;


import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Para poder transformar em tabela
@Entity

//Para poder nomear
@Table(name= "empresas")

//Getters, setters e construtores:


// sem eles, eu teria que escrever para cada campo: 
//public String getNome() { return nome; }
//public void setNome(String nome) { this.nome = nome; }

@Getter
@Setter

//Para construtor que aceita todos os atributos da classe como parametros
@AllArgsConstructor

//Para construtor vazio
@NoArgsConstructor

public class Empresa {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	 	
	 	// Usa o @column porque quer mudar algo que viria como default (o default seria que aceita nulo) 
	 	@Column(nullable = false)
	    private String nome;
	    
	    // Pois não pode ter 2 iguais e não pode ficar em branco
	    @Column(unique =true, nullable = false)
	    private String cnpj;
	    
	    @Column(unique =true, nullable = false)
	    private String email;
	    private boolean ativo;
	    
	    // MAPPEDBY - indica que a sua entidade é o lado inverso (ou lado não dominante) de um relacionamento e que o mapeamento da 
	    // chave estrangeira já foi definido pelo lado dono.
	    // lado inverso do ManyToMany — mappedBy aponta para o campo "empresas" que está no Usuario
	    // sem isso o JPA não sabe que essa relação já existe e tentaria criar uma segunda tabela intermediária
	    @ManyToMany(mappedBy = "empresas")
	    private List<Usuario> usuarios;

}
