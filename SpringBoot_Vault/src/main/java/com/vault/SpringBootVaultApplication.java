package com.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;

@SpringBootApplication
@EnableConfigurationProperties(MyConfiguration.class)
public class SpringBootVaultApplication implements CommandLineRunner {

	private final MyConfiguration configuration;

	@Autowired
	private VaultTemplate vaultTemplate;

	public SpringBootVaultApplication(MyConfiguration configuration) {
		this.configuration = configuration;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootVaultApplication.class, args);
	}

	@Override
	public void run(String... args) {
		/*
		 * 
		 * Logger logger = LoggerFactory.getLogger(SpringBootVaultApplication.class);
		 * 
		 * logger.info("----------------------------------------");
		 * logger.info("Configuration properties");
		 * logger.info("   example.username is {}", configuration.getUsername());
		 * logger.info("   example.password is {}", configuration.getPassword());
		 * logger.info("----------------------------------------");
		 */

		// You usually would not print a secret to stdout
		VaultResponse response = vaultTemplate.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");
		System.out.println("Value of github.oauth2.key");
		System.out.println("-------------------------------");
		System.out.println(response.getData().get("github.oauth2.key"));
		System.out.println("-------------------------------");
		System.out.println();

		// Let's encrypt some data using the Transit backend.
		VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();

		// We need to setup transit first (assuming you didn't set up it yet).
		VaultSysOperations sysOperations = vaultTemplate.opsForSys();

		if (!sysOperations.getMounts().containsKey("transit/")) {

			sysOperations.mount("transit", VaultMount.create("transit"));

			transitOperations.createKey("foo-key");
		}

		// Encrypt a plain-text value
		String ciphertext = transitOperations.encrypt("foo-key", "Secure message");

		System.out.println("Encrypted value");
		System.out.println("-------------------------------");
		System.out.println(ciphertext);
		System.out.println("-------------------------------");
		System.out.println();

		// Decrypt

		String plaintext = transitOperations.decrypt("foo-key", ciphertext);

		System.out.println("Decrypted value");
		System.out.println("-------------------------------");
		System.out.println(plaintext);
		System.out.println("-------------------------------");
		System.out.println();
	}
}
