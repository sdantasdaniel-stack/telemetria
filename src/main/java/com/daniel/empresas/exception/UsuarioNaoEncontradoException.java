package com.daniel.empresas.exception;

//existem os Error e Exception. Error Representa problemas graves da JVM que o seu código não deve tentar tratar.

//Se acontecer, o programa geralmente encerra. Você nunca vai criar classes que estendem Error. 

//As exceções se dividem em: Checked exceptions — o compilador te obriga a tratar ou declarar. Exemplos: IOException, SQLException. 

//Se você chamar um método que lança uma checked exception e não tratar, o código não compila.

//Unchecked exceptions — herdam de RuntimeException. O compilador não obriga tratamento. Podem ser lançadas em qualquer lugar sem declaração.

//RuntimeException É a classe pai de todas as exceções unchecked. Ela existe para representar situações que são erros de programação ou de 
//lógica — coisas que não deveriam acontecer se o código estiver correto.

//O RunTimeException serve para você não ter que tratar a exceção em todo lugar que chamar o Service — o GlobalExceptionHandler já cuida disso 
//centralmente. Estendendo RuntimeException, a exceção sobe pela pilha livremente até o GlobalExceptionHandler interceptar.

public class UsuarioNaoEncontradoException extends RuntimeException {
	
	//construtor que recebe uma string mensagem
	
	// O super(mensagem) repassa a mensagem para o construtor da RuntimeException — que é a classe pai. É ela que vai guardar essa mensagem
	// internamente para o GlobalExceptionHandler conseguir recuperar depois.
		
	public UsuarioNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}
