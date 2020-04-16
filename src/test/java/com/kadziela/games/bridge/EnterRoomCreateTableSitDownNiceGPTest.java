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
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnterRoomCreateTableSitDownNiceGPTest 
{
	private static final Logger logger = LogManager.getLogger(EnterRoomCreateTableSitDownNiceGPTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	 
	 @Autowired TableService tableService;

	 private BlockingQueue<Map<String,Object>> messageQueue = new LinkedBlockingQueue<Map<String,Object>>();
	 
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
			sitPlayer("North","NORTH",String.valueOf(tableId),stompSession);
			sitPlayer("South","SOUTH",String.valueOf(tableId),stompSession);
			sitPlayer("East","EAST",String.valueOf(tableId),stompSession);
			List<Map<String,Object>> messages = sitPlayer("West","WEST",String.valueOf(tableId),stompSession);						
			Map<String,Object> mm = messages.get(1);
			assertNotNull(mm.get("cardsBySeat"));			
			mm = messages.get(2);
			assertNotNull(mm.get("dealer"));						
	 }
	 private List<Map<String,Object>> sitPlayer(String name, String position, String table, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put("playerName", name);
			attributes.put("tableId", table);
			attributes.put("position", position);
			stompSession.send("/app/table/sitDown", attributes);
			List<Map<String,Object>> messages = TestUtils.queueToList(messageQueue);
			logger.info("Player {} attempted to sit in the {} seat at table {}, the /topic/table/{} channel received the following messages: {} ",name,position,table,table,messages);
			assertNotNull(messages);
			Map<String,Object> mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(4, mm.size());
			assertEquals(name, mm.get("playerName"));
			assertEquals(position, mm.get("position"));
			assertEquals(table, mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			return messages;
	 }
	 private Long enterRoomAndCreateTable(StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
			stompSession.subscribe("/topic/room", new QueueStompFrameHandler());			
			enterPlayer("North", stompSession);
			enterPlayer("South", stompSession);
			enterPlayer("East", stompSession);
			enterPlayer("West", stompSession);			
			Long externalId = System.currentTimeMillis();
			stompSession.send("/app/table/openNewWithExternalId", externalId);
			List<Map<String,Object>> messages = TestUtils.queueToList(messageQueue);
			assertNotNull(messages);
			Map<String,Object> map = messages.get(0);						
			logger.info("Attempted to open a new table with external id {}, this message was sent to the /topic/room channel: {} ",externalId,map);
			assertNotNull(map);
			Table table = tableService.findByExternalId(externalId);
			logger.info("table = {} ", table);
			return table.getId();
	 }
	 private void enterPlayer(String name, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			stompSession.send("/app/room/enter", name);
			List<Map<String,Object>> messages = TestUtils.queueToList(messageQueue);
			logger.info("Player {} attempted to enter the room, this message was sent to the /topic/room channel: {} ",name,messages);
			assertNotNull(messages);
			Map<String,Object> map = messages.get(0);
			List players = (List) map.get("players");
			assertNotNull(players);
			String name2 = null;
			for (Object player:players)
			{
				name2 = (String) ((Map) player).get("name");
				if (name2.contentEquals(name)) break;
			}
			assertEquals(name,name2);		 
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