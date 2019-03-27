package com.shentu.dht;

import com.shentu.dht.server.UDPServer;
import com.shentu.dht.task.FindNodeTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SpiderApplication implements CommandLineRunner {

	private UDPServer udpServer;
	private FindNodeTask findNodeTask;

	public SpiderApplication(UDPServer udpServer,FindNodeTask findNodeTask){
		this.udpServer=udpServer;
		this.findNodeTask=findNodeTask;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpiderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info(Thread.currentThread().getContextClassLoader().getResource("node.txt").getPath()+"======path");
		udpServer.run();
		findNodeTask.run();
	}
}
