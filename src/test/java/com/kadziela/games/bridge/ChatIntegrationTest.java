package com.kadziela.games.bridge;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import com.kadziela.games.bridge.model.Message;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatIntegrationTest 
{
	private static final Logger logger = LogManager.getLogger(ChatIntegrationTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	 
	 private CompletableFuture<Message> completableFuture;
	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/kadziela-bridge-websocket";
	
	    WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
	    stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	
	    StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
	
	    stompSession.subscribe("/topic/chat", new MessageStompFrameHandler());
	    stompSession.send("/app/chat", new Message("Hello"));
	
	    Message message = completableFuture.get(10, TimeUnit.SECONDS);
	    logger.info(message);
	    assertNotNull(message);
	}
	 private List<Transport> createTransportClient() 
	 {
	 	List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
	 private class MessageStompFrameHandler implements StompFrameHandler 
	 {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            logger.info(stompHeaders.toString());
            return Message.class;
        }
        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
        	logger.info((Message) o);
            completableFuture.complete((Message) o);
        }
    }
}