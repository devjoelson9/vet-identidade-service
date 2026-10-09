package br.com.vet.identidade;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VetIdentidadeServiceApplication {
	public static void main(String[] args) {
        // Carrega o .env e define as chaves automaticamente no System.setProperty
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
        SpringApplication.run(VetIdentidadeServiceApplication.class, args);
	}

}
