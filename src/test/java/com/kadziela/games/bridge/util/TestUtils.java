package com.kadziela.games.bridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.kadziela.games.bridge.EnterRoomCreateTableSitDownNiceGPTest;
import com.kadziela.games.bridge.handlers.ErrorStompFrameHandler;
import com.kadziela.games.bridge.handlers.QueueStompFrameHandler;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.TableService;

public class TestUtils 
{
	private static final Logger logger = LogManager.getLogger(TestUtils.class);

	private TestUtils() {}
	
	 public static List<Map<String,Object>> queueToList(BlockingQueue<Map<String,Object>> queue) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		List<Map<String,Object>> messages = new ArrayList<Map<String,Object>>();
		while (true)
		{
			Map<String,Object> map2 = queue.poll(5, TimeUnit.SECONDS);
			if (map2 != null) 
			{
				messages.add(map2);
			}
			else break;
		}
		return messages;
	 }
	 public static final void bid(ValidBidOption bid,Long tableId, SeatPosition position, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 Map<String,String> attributes = new HashMap<String,String>();
		 attributes.put("tableId", tableId.toString());
		 attributes.put("bid", bid.toString());
		 attributes.put("position", position.toString());
		 stompSession.send("/app/contract/bid", attributes);
		 Thread.sleep(1000);		 
	 }
	 public static final List<Transport> createTransportClient() 
	 {
	 	List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
	 }
	 public static final StompSession setupTest(String URL, int port, ErrorStompFrameHandler errorStompFrameHandler,QueueStompFrameHandler queueStompFrameHandler) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		URL = "ws://localhost:" + port + "/kadziela-bridge-websocket";		
		WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(TestUtils.createTransportClient()));
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());		
		StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);		
		stompSession.subscribe("/topic/errors/", errorStompFrameHandler);
		stompSession.subscribe("/topic/room", queueStompFrameHandler);			
		return stompSession;
	 }
	 public static final void roomEnterPlayer(String name, StompSession stompSession,QueueStompFrameHandler queueStompFrameHandler, ErrorStompFrameHandler errorStompFrameHandler) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		stompSession.send("/app/room/enter", name);
		List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
		logger.info("Player {} attempted to enter the room, this message was sent to the /topic/room channel: {} ",name,messages);
		assertNotNull(messages);
		Map<String,Object> map = messages.get(0);
		List<Map<String,String>> players = (List<Map<String,String>>) map.get("players");
		assertNotNull(players);
		String name2 = null;
		for (Map<String,String> player:players)
		{
			name2 = player.get("name");
			if (name2.contentEquals(name)) break;
		}
		assertEquals(name,name2);
		stompSession.subscribe("/queue/private/"+name, errorStompFrameHandler);					
	 }
	 public static final void roomEnterPlayers(StompSession stompSession, QueueStompFrameHandler queueStompFrameHandler, ErrorStompFrameHandler errorStompFrameHandler, String ... names) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 Stream.of(names).forEach(name -> {
			try {roomEnterPlayer(name,stompSession,queueStompFrameHandler,errorStompFrameHandler);} 
			catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) 
			{
				logger.error("exception occurred while entering players into the room ",e);
			}
		});
	 }
	 public static final Long createTable(StompSession stompSession, QueueStompFrameHandler queueStompFrameHandler, TableService tableService) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
		 Long externalId = System.currentTimeMillis();
		 stompSession.send("/app/table/openNewWithExternalId", externalId);
		 List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
		 assertNotNull(messages);
		 Map<String,Object> map = messages.get(0);						
		 logger.info("Attempted to open a new table with external id {}, this message was sent to the /topic/room channel: {} ",externalId,map);
		 assertNotNull(map);
		 Table table = tableService.findByExternalId(externalId);
		 logger.info("table = {} ", table);
		 stompSession.subscribe("/topic/table/"+table.getId(), queueStompFrameHandler);
		 return table.getId();
	 }
	 public static final List<Map<String,Object>> sitPlayer(String name, String position, String table, StompSession stompSession, QueueStompFrameHandler queueStompFrameHandler) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
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
	 public static final SeatPosition sit4PlayersDown(Long tableId, StompSession stompSession, QueueStompFrameHandler queueStompFrameHandler, String ... names) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			TestUtils.sitPlayer(names[0],"NORTH",String.valueOf(tableId),stompSession,queueStompFrameHandler);
			TestUtils.sitPlayer(names[1],"SOUTH",String.valueOf(tableId),stompSession,queueStompFrameHandler);
			TestUtils.sitPlayer(names[2],"EAST",String.valueOf(tableId),stompSession,queueStompFrameHandler);
			List<Map<String,Object>> messages = TestUtils.sitPlayer(names[3],"WEST",String.valueOf(tableId),stompSession,queueStompFrameHandler);						
			Map<String,Object> mm = messages.get(1);
			assertNotNull(mm.get("cardsBySeat"));			
			mm = messages.get(2);
			assertNotNull(mm.get("dealer"));
			int dealerSelectedMessageCount = 0;
			SeatPosition dealer = null;
			for (Map<String,Object> map:messages)
			{
				String message = (String) map.get("message");
				if (message != null && message.contains("current dealer is ")) 
				{
					dealerSelectedMessageCount++;
					dealer = SeatPosition.valueOf((String) map.get("dealer"));
				}
			}
			assertEquals(1, dealerSelectedMessageCount);
			assertNotNull(dealer);
			return dealer;
	 }
	 public static final Long put4PlayersInRoomAndTable(StompSession stompSession, ErrorStompFrameHandler errorStompFrameHandler,QueueStompFrameHandler queueStompFrameHandler, TableService tableService, String ... names) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 TestUtils.roomEnterPlayers(stompSession, queueStompFrameHandler,errorStompFrameHandler, names);
		 Long tableId = TestUtils.createTable(stompSession,queueStompFrameHandler,tableService);	    
		 TestUtils.sit4PlayersDown(tableId, stompSession, queueStompFrameHandler, names);	
		 return tableId;
	 }
	 public static final void playCard(Card card, Long tableId, SeatPosition position, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		Map<String,String> attributes = new HashMap<String,String>();
		attributes.put("suit", card.getSuit().toString());
		attributes.put("rank", card.getRank().toString());
		attributes.put("tableId", tableId.toString());
		attributes.put("position", position.toString());
		stompSession.send("/app/play/card", attributes);
		Thread.sleep(1000);		 
	 }
}