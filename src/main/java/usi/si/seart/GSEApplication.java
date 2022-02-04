package usi.si.seart;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import usi.si.seart.gseapp.controller.GitRepoController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class GSEApplication {
	public static void main(String[] args) {
		SpringApplication.run(GSEApplication.class, args);
	}

	@PostConstruct
	private void createDownloadsFolder() {
		String path = GitRepoController.downloadFolder;
		log.info("Creating downloads folder: ./{}", path);
		try {
			FileUtils.forceMkdir(new File(path));
		} catch (IOException ex) {
			log.error("Could not create downloads folder!", ex);
		}
	}

	@PreDestroy
	private void deleteDownloadsFolder() {
		String path = GitRepoController.downloadFolder;
		log.info("Clearing downloads folder: ./{}", path);
		try {
			FileUtils.forceDelete(new File(path));
		} catch (IOException ex) {
			log.error("Could not clear downloads folder!", ex);
		}
	}
}
