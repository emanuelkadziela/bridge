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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnterRoomCreateTableSitDownNiceGPTest 
{
	private static final Logger logger = LogManager.getLogger(EnterRoomCreateTableSitDownNiceGPTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	 
	 private CompletableFuture<Map> messageFuture = new CompletableFuture<>();
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
			stompSession.subscribe("/topic/table/"+tableId, new SitDownStompFrameHandler());

			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put("playerName", "North");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "NORTH");
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
			logger.info("Player North attempted to sit in the NORTH seat at table {}, the /topic/table/{} channel received the following messages: {} ",tableId,tableId,messages);
			assertNotNull(messages);
			assertEquals(1, messages.size());
			Map mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(4, mm.size());
			assertEquals("North", mm.get("playerName"));
			assertEquals("NORTH", mm.get("position"));
			assertEquals(String.valueOf(tableId), mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "South");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "SOUTH");
			stompSession.send("/app/table/sitDown", attributes);
			messages = new ArrayList<Map>();
			while (true)
			{
				Map map = messageQueue.poll(5, TimeUnit.SECONDS);
				if (map != null) 
				{
					messages.add(map);
				}
				else break;
			}
			logger.info("Player South attempted to sit in the SOUTH seat at table {}, the /topic/table/{} channel received the following messages: {} ",tableId,tableId,messages);
			assertNotNull(messages);
			assertEquals(1, messages.size());
			mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(4, mm.size());
			assertEquals("South", mm.get("playerName"));
			assertEquals("SOUTH", mm.get("position"));
			assertEquals(String.valueOf(tableId), mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "East");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "EAST");
			stompSession.send("/app/table/sitDown", attributes);
			messages = new ArrayList<Map>();
			while (true)
			{
				Map map = messageQueue.poll(5, TimeUnit.SECONDS);
				if (map != null) 
				{
					messages.add(map);
				}
				else break;
			}
			logger.info("Player East attempted to sit in the EAST seat at table {}, the /topic/table/{} channel received the following messages: {} ",tableId,tableId,messages);
			assertNotNull(messages);
			assertEquals(1, messages.size());
			mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(4, mm.size());
			assertEquals("East", mm.get("playerName"));
			assertEquals("EAST", mm.get("position"));
			assertEquals(String.valueOf(tableId), mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			
			attributes = new HashMap<String,String>();
			attributes.put("playerName", "West");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "WEST");
			stompSession.send("/app/table/sitDown", attributes);
			messages = new ArrayList<Map>();
			while (true)
			{
				Map map = messageQueue.poll(5, TimeUnit.SECONDS);
				if (map != null) 
				{
					messages.add(map);
				}
				else break;
			}
			logger.info("Player West attempted to sit in the WEST seat at table {}, the /topic/table/{} channel received the following messages: {} ",tableId,tableId,messages);
			assertNotNull(messages);
			assertEquals(3, messages.size());
			mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(4, mm.size());
			assertEquals("West", mm.get("playerName"));
			assertEquals("WEST", mm.get("position"));
			assertEquals(String.valueOf(tableId), mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			
			mm = messages.get(1);
			assertNotNull(mm.get("cardsBySeat"));
			
			mm = messages.get(2);
			assertNotNull(mm.get("dealer"));			
			
	 }
	 private Long enterRoomAndCreateTable(StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
			stompSession.subscribe("/topic/room", new EnterRoomStompFrameHandler());
			
			stompSession.send("/app/room/enter", "North");
			Map map = messageFuture.get(10, TimeUnit.SECONDS);
			logger.info("Player North attempted to enter the room, this message was sent to the /topic/room channel: {} ",map);
			assertNotNull(map);
			List players = (List) map.get("players");
			assertNotNull(players);
			assertEquals(1,players.size());
			String name = (String) ((Map) players.get(0)).get("name");
			assertEquals("North",name);

			messageFuture = new CompletableFuture<Map>();			
			stompSession.send("/app/room/enter", "South");
			map = messageFuture.get(10, TimeUnit.SECONDS);
			logger.info("Player South attempted to enter the room, this message was sent to the /topic/room channel: {} ",map);
			assertNotNull(map);
			players = (List) map.get("players");
			assertNotNull(players);
			assertEquals(2,players.size());
			for (Object player:players)
			{
				name = (String) ((Map) player).get("name");
				if (name.contentEquals("South")) break;
			}
			assertEquals("South",name);

			messageFuture = new CompletableFuture<Map>();			
			stompSession.send("/app/room/enter", "East");
			map = messageFuture.get(10, TimeUnit.SECONDS);
			logger.info("Player East attempted to enter the room, this message was sent to the /topic/room channel: {} ",map);
			assertNotNull(map);
			players = (List) map.get("players");
			assertNotNull(players);
			assertEquals(3,players.size());
			for (Object player:players)
			{
				name = (String) ((Map) player).get("name");
				if (name.contentEquals("East")) break;
			}
			assertEquals("East",name);

			messageFuture = new CompletableFuture<Map>();
			stompSession.send("/app/room/enter", "West");
			map = messageFuture.get(10, TimeUnit.SECONDS);
			logger.info("Player West attempted to enter the room, this message was sent to the /topic/room channel: {} ",map);
			assertNotNull(map);
			players = (List) map.get("players");
			assertNotNull(players);
			assertEquals(4,players.size());
			for (Object player:players)
			{
				name = (String) ((Map) player).get("name");
				if (name.contentEquals("West")) break;
			}
			assertEquals("West",name);

			messageFuture = new CompletableFuture<Map>();
			stompSession.send("/app/table/openNew", null);
			map = messageFuture.get(10, TimeUnit.SECONDS);
			logger.info("Attempted to open a new table, this message was sent to the /topic/room channel: {} ",map);
			assertNotNull(map);
			List ids = (List) map.get("tableIds");
			assertEquals(1,ids.size());
			Long tableId = (Long) ids.get(0);
			logger.info("tableId = {}",tableId);
			return tableId;
	 }
	 private List<Transport> createTransportClient() 
	 {
	 	List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
	 }
	 private class EnterRoomStompFrameHandler implements StompFrameHandler 
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
			messageFuture.complete((Map) o);
		}
	 }
	 private class SitDownStompFrameHandler implements StompFrameHandler 
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