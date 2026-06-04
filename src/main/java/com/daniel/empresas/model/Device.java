package com.daniel.empresas.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


//Para poder transformar em tabela
@Entity

//Para poder nomear
@Table(name= "devices")

//Getters, setters e construtores:

//sem eles, eu teria que escrever para cada campo: 
//public String getNome() { return nome; }
//public void setNome(String nome) { this.nome = nome; }

@Getter
@Setter

//Para construtor que aceita todos os atributos da classe como parametros
@AllArgsConstructor

//Para construtor vazio
@NoArgsConstructor

public class Device {
	
	// chave primária/gerada automaticamente
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 	
	// Usa o @column porque quer mudar algo que viria como default (o default seria que aceita nulo) 
 	@Column(nullable = false)
    private String nome;
    
    // Pois não pode ter 2 iguais e não pode ficar em branco
    @Column(unique =true, nullable = false)
    private String identificador;
    
    //EnumType.STRING - para o hibernate saber que é para salvar como online ou offline ao invés de número
    @Enumerated(EnumType.STRING)
    private StatusEnum status;
    
    private boolean ativo;
    
   
    // tipo do relacionamento entre as tabelas(muitos devices podem pertencer a uma empresa)
    @ManyToOne
    
    // para dizer que é chave estrangeira
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
    
    //@ManyToOne — não precisa de tabela intermediária porque a referência fica direto na tabela do lado "muitos". No seu caso, a tabela 
    //devices ganha simplesmente uma coluna empresa_id, que aponta para o ID da empresa dona daquele device.
    
    // adicionados para fornecer a localização de cada device
    private Double latitude;
    private Double longitude;
    
}
