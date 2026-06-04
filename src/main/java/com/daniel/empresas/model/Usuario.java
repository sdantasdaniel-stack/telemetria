package com.daniel.empresas.model;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;


//Para poder transformar em tabela
@Entity

//Para poder nomear
@Table(name= "usuarios")

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

// UserDetails - interface do Spring security que representa um usuário autenticado
public class Usuario implements UserDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 	
	// Usa o @column porque quer mudar algo que viria como default (o default seria que aceita nulo) 
 	@Column(nullable = false)
    private String nome;
    
    // Pois não pode ter 2 iguais e não pode ficar em branco
    @Column(unique =true, nullable = false)
    private String email;
    
    
    @Column(nullable = false)
    private String senha;
    
    //para o hibernate saber que é para salvar como admin ou user ao invés de número
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    
    private boolean ativo;
    
   
    @ManyToMany // Um usuário pode ter várias empresas, e uma empresa pode ter vários usuários
    @JoinTable( // Define a tabela intermediária que faz a ligação entre as duas entidades
        name = "usuario_empresa", // Nome da tabela intermediária no banco
        joinColumns = @JoinColumn(name = "usuario_id"), // Coluna que referencia o ID do usuário (lado "dono" da relação)
        inverseJoinColumns = @JoinColumn(name = "empresa_id") // Coluna que referencia o ID da empresa (lado "inverso")
    )
    private List<Empresa> empresas; // Lista de empresas vinculadas a esse usuário

    // UserDetails tem 7 métodos. Esses 3 de baixo são obrigatórios de se declarar. Os outros 4 tem um default = true se nao forem declarados.
    // Desses 4, preciso alterar o default de isEnabled() para "ativo", pois é essa variável que determina se o usuário pode logar ou não.
	
	// Os outros 3 métodos:
	// isAccountNonExpired() Deve retornar true — pois não tem controle de expiração de conta no seu sistema.
	//isAccountNonLocked() Deve retornar true — você não tem controle de bloqueio de conta.
	//isCredentialsNonExpired() Deve retornar true — você não tem controle de expiração de credenciais.
    
    
    
    // O método retorna Collection e não List porque o contrato da interface UserDetails foi definido assim
    
    // Collection é uma interface raiz do Java que representa qualquer grupo de objetos. 
    
    // List é uma interface que estende Collection — ela é uma Collection que: tem ordem, permite duplicatas, e permite acessar elementos
    //por índice.
    //GrantedAuthority é a interface do spring security que representa uma permissão no sistema.
    
    // Para o Spring Security entender o role do seu usuário, você precisa converter o RoleEnum para um objeto que implemente GrantedAuthority
    
    // List.of(role) - Cria uma lista imutável com um único elemento — o role do usuário.
    
    // .stream()Transforma a lista em um Stream — uma sequência de elementos que você pode processar em cadeia. Permite operações funcionais
    //  'por exemplo' map,filter e collect.
    
    // .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())):
    
    //Para cada elemento do stream — no caso só um — transforma o RoleEnum em um SimpleGrantedAuthority. O SimpleGrantedAuthority é uma
    //implementação de GrantedAuthority que recebe uma String.
    
    // .collect(Collectors.toList()):
    
    //Coleta os elementos do stream de volta para uma List. É o encerramento da operação — o stream não é uma coleção, que é o tipo de retorno
    // que o método espera, então você precisa converter de volta.
    
    //Anotação usada para sobreescrever um método da interface implementada
    @Override
    //? é chamado de wildcard (curinga) e significa "qualquer tipo"
    
    // ? extends GrantedAuthority significa "qualquer tipo que seja GrantedAuthority ou uma subclasse dela". Então o método aceita tanto uma
    // Collection<GrantedAuthority> quanto uma Collection<SimpleGrantedAuthority>
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(role)
		        .stream()
		        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
		        .collect(Collectors.toList());
	}


	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return senha;
	}


	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}
	
	@Override
	public boolean isEnabled() {
		
		return ativo;
	}
    
    

}
