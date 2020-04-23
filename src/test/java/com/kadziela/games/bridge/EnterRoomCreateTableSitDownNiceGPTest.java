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
import com.kadziela.games.bridge.handlers.ErrorStompFrameHandler;
import com.kadziela.games.bridge.handlers.QueueStompFrameHandler;
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
	 
	 @Autowired private TableService tableService;
	 @Autowired private QueueStompFrameHandler queueStompFrameHandler; 
	 @Autowired private ErrorStompFrameHandler errorStompFrameHandler; 
	 	 	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
		StompSession stompSession = TestUtils.setupTest(URL, port,errorStompFrameHandler,queueStompFrameHandler);
		Long tableId = enterRoomAndCreateTable(stompSession);	    
		sitDown(tableId,stompSession);		
	 }
	 private void sitDown(Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			stompSession.subscribe("/topic/table/"+tableId, queueStompFrameHandler);
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
			List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
			logger.info("Player {} attempted to sit in the {} seat at table {}, the /topic/table/{} channel received the following messages: {} ",name,position,table,table,messages);
			assertNotNull(messages);
			Map<String,Object> mm = messages.get(0);
			assertNotNull(mm);
			assertEquals(name, mm.get("playerName"));
			assertEquals(position, mm.get("position"));
			assertEquals(table, mm.get("tableId"));
			assertTrue(((String) mm.get("message")).contains("successfully sat down"));
			return messages;
	 }
	 private Long enterRoomAndCreateTable(StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
		 	TestUtils.roomEnterPlayer("North", stompSession,queueStompFrameHandler);
		 	TestUtils.roomEnterPlayer("South", stompSession,queueStompFrameHandler);
		 	TestUtils.roomEnterPlayer("East", stompSession,queueStompFrameHandler);
		 	TestUtils.roomEnterPlayer("West", stompSession,queueStompFrameHandler);
		 	Long externalId = System.currentTimeMillis();
			stompSession.send("/app/table/openNewWithExternalId", externalId);
			List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
			assertNotNull(messages);
			Map<String,Object> map = messages.get(0);						
			logger.info("Attempted to open a new table with external id {}, this message was sent to the /topic/room channel: {} ",externalId,map);
			assertNotNull(map);
			Table table = tableService.findByExternalId(externalId);
			logger.info("table = {} ", table);
			return table.getId();
	 }
}