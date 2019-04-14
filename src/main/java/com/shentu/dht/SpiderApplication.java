package com.shentu.dht;

import com.shentu.dht.bcodec.Bencode;
import com.shentu.dht.client.ESClient;
import com.shentu.dht.peer.RoutingTable;
import com.shentu.dht.server.UDPServer;
import com.shentu.dht.task.FindMetaDataTask;
import com.shentu.dht.task.FindNodeTask;
import io.netty.bootstrap.Bootstrap;
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

	private FindMetaDataTask findMetaDataTask;

	private Bootstrap bootstrap;

	private Bencode bencode;

	private RoutingTable routingTable;

	private ESClient esClient;

	public SpiderApplication(UDPServer udpServer,FindNodeTask findNodeTask,Bootstrap bootstrap,Bencode bencode,FindMetaDataTask findMetaDataTask,ESClient esClient){
		this.udpServer=udpServer;
		this.findNodeTask=findNodeTask;
		this.bootstrap=bootstrap;
		this.bencode=bencode;
//		this.routingTable=routingTable;
		this.findMetaDataTask=findMetaDataTask;
		this.esClient=esClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpiderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
//		udpServer.run();
		findNodeTask.run();
		findMetaDataTask.run();
//		bootstrap.clone().handler(new ChannelInitializer() {
//			@Override
//			protected void initChannel(Channel channel) throws Exception {
//				channel.pipeline()
//						.addLast(new ReadTimeoutHandler(15))
//						.addLast(new SearchMetaDataHandler(bencode, routingTable,esClient, "7630af62cb025e3c06dd2c43c82a94cd323d31e5"));
//
//			}
//		}).connect(new InetSocketAddress("112.185.190.115", 22560));
	}
}
