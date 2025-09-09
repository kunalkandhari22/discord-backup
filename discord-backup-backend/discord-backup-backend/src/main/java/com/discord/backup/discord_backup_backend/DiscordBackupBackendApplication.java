package com.discord.backup.discord_backup_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiscordBackupBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordBackupBackendApplication.class, args);
	}

}
