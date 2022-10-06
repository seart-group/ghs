package usi.si.seart;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
@SpringBootApplication
public class GSEApplication {

	String exportFolder;

	public static void main(String[] args) {
		SpringApplication.run(GSEApplication.class, args);
	}

	@PostConstruct
	private void createDownloadsFolder() {
		log.info("Creating downloads folder: ./{}", exportFolder);
		try {
			FileUtils.forceMkdir(new File(exportFolder));
		} catch (IOException ex) {
			log.error("Could not create downloads folder!", ex);
		}
	}

	@PreDestroy
	private void deleteDownloadsFolder() {
		log.info("Clearing downloads folder: ./{}", exportFolder);
		try {
			FileUtils.forceDelete(new File(exportFolder));
		} catch (IOException ex) {
			log.error("Could not clear downloads folder!", ex);
		}
	}
}
