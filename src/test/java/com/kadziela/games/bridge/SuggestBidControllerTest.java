package com.kadziela.games.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.kadziela.games.bridge.util.CardUtils;
import com.kadziela.games.bridge.util.HandGenerator;
import com.kadziela.games.bridge.util.HandUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SuggestBidControllerTest 
{
	private static final Logger logger = LogManager.getLogger(SuggestBidControllerTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	 
	 private CompletableFuture<Map> completableFuture;
	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/kadziela-bridge-websocket";
	
	    WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
	    stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	
	    StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
	
	    stompSession.subscribe("/topic/bid/suggest", new MessageStompFrameHandler());

	    stompSession.send("/app/bid/suggest", CardUtils.convertFromEnumsToStrings(HandGenerator.generateAShittyHand()));
	    Map message = completableFuture.get(10, TimeUnit.SECONDS);
	    logger.info(message);
	    assertNotNull(message);
	    assertEquals("PASS",message.get("message"));

	    completableFuture = new CompletableFuture<>();
	    stompSession.send("/app/bid/suggest", HandGenerator.generateA1NTHand());
	    message = completableFuture.get(10, TimeUnit.SECONDS);
	    logger.info(message);
	    assertNotNull(message);
	    assertEquals("ONE_NO_TRUMP",message.get("message"));
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
            return Map.class;
        }
        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
        	logger.info((Map) o);
            completableFuture.complete((Map) o);
        }
    }
}