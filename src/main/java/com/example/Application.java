package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Component
	public class AppStartupRunner implements ApplicationRunner {

		private TableWatcher tableWatcher;

		public AppStartupRunner(TableWatcher tableWatcher) {
			this.tableWatcher = tableWatcher;
		}

		@Override
		public void run(ApplicationArguments args) {
			tableWatcher.activate();
		}

		@PreDestroy
		public void destroy() {
			tableWatcher.deactivate();
		}

	}

}
