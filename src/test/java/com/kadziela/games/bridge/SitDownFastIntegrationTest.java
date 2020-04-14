package com.kadziela.games.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.gson.Gson;
import com.kadziela.games.bridge.model.Message;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.service.TableService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SitDownFastIntegrationTest 
{
	private static final Logger logger = LogManager.getLogger(SitDownFastIntegrationTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	
	 @Autowired TableService tableService;
	 
	 private BlockingQueue<Map> messageQueue = new LinkedBlockingQueue<Map>();
	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
		URL = "ws://localhost:" + port + "/kadziela-bridge-websocket";		
		WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());		
		StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);

		stompSession.subscribe("/topic/errors/", new ErrorStompFrameHandler());
		
		Long tableId = enterRoomAndCreateTable(stompSession);	    
		sitDown(tableId,stompSession);
		
	 }
	 private void sitDown(Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			stompSession.subscribe("/topic/table/"+tableId, new QueueStompFrameHandler());
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put("playerName", "N1");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "NORTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "S1");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "SOUTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "E1");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "EAST");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "W1");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "WEST");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "N2");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "NORTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "S2");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "SOUTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "E2");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "EAST");
			stompSession.send("/app/table/sitDown", attributes);
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "W2");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "WEST");
			stompSession.send("/app/table/sitDown", attributes);
			
			List<Map> messages = new ArrayList<Map>();
			while (true)
			{
				Map map = messageQueue.poll(5, TimeUnit.SECONDS);
				if (map != null) 
				{
					messages.add(map);
				}
				else break;
			}
			logger.info("attempted to quickly seat a lot of players at one table, here are the messages: {}",messages);
			assertEquals(4,tableService.findById(tableId).playersSitting());
			int dealerSelectedMessageCount = 0;
			for (Map map:messages)
			{
				if (map.get("message") != null && ((String) map.get("message")).contains("current dealer is ")) dealerSelectedMessageCount++;
			}
			assertEquals(1, dealerSelectedMessageCount);
	 }
	 private Long enterRoomAndCreateTable(StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
			stompSession.subscribe("/topic/room", new QueueStompFrameHandler());
			
			stompSession.send("/app/room/enter", "N1");
			stompSession.send("/app/room/enter", "S1");
			stompSession.send("/app/room/enter", "E1");
			stompSession.send("/app/room/enter", "W1");
			
			stompSession.send("/app/room/enter", "N2");
			stompSession.send("/app/room/enter", "S2");
			stompSession.send("/app/room/enter", "E2");
			stompSession.send("/app/room/enter", "W2");
			
			stompSession.send("/app/table/openNew", null);
			List<Map> messages = new ArrayList<Map>();
			while (true)
			{
				Map map = messageQueue.poll(5, TimeUnit.SECONDS);
				if (map != null) 
				{
					messages.add(map);
				}
				else break;
			}
			logger.info("Attempted to open a new table, this message was sent to the /topic/room channel: {} ",messages);
			assertNotNull(messages);
			Long tableId = 0l;
			for (Map map:messages)
			{
				if (map.containsKey("tableIds"))
				{
					Collection tIds = (Collection) map.get("tableIds");
					for (Object id:tIds)
					{
						Long lId = (Long) id;
						if (lId > tableId) tableId = lId;
					}
				}
			}
			logger.info("tableId = {}",tableId);
			return tableId;
	 }
	 private List<Transport> createTransportClient() 
	 {
	 	List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
	 }
	 private class QueueStompFrameHandler implements StompFrameHandler 
	 {
	 	@Override
		public Type getPayloadType(StompHeaders stompHeaders) 
		{
		    logger.info("stompHeaders: {}",stompHeaders.toString());
		    return Map.class;
		}
		@Override
		public void handleFrame(StompHeaders stompHeaders, Object o)
		{
			logger.info("handleFrame message payload: {} ",(Map) o);
			try 
			{
				messageQueue.offer((Map) o, 5, TimeUnit.SECONDS);			
			}
			catch(InterruptedException ie) 
			{
				logger.error(ie);
			}
		}
	 }
	 private class ErrorStompFrameHandler implements StompFrameHandler 
	 {
	 	@Override
		public Type getPayloadType(StompHeaders stompHeaders) 
		{
		    logger.info(stompHeaders.toString());
		    return Map.class;
		}
		@Override
		public void handleFrame(StompHeaders stompHeaders, Object o) 
		{
			logger.error((Map) o);
		}
	 }
}