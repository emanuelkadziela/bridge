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
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Message;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayIntegrationTest 
{
	private static final Logger logger = LogManager.getLogger(PlayIntegrationTest.class);
	
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
		SeatPosition dealer = sitDown(tableId,stompSession);
		logger.info("the dealer is {}",dealer);
		doSimpleBid(dealer, tableId, stompSession);
		Map<SeatPosition,Map<Suit,List<Card>>> cards = getCardMap(tableService.findById(tableId));
		logger.info("cards by position and suit = {} ", cards);
	 }
	 private Map<SeatPosition, Map<Suit, List<Card>>> getCardMap(Table table) 
	 {
		 Map<SeatPosition,Map<Suit,List<Card>>> result = new HashMap<>();
		 for(SeatPosition sp:SeatPosition.values())
		 {
			 Map<Suit,List<Card>> inner = new HashMap<>();
			 for (Card card:table.getPlayerAtPosition(sp).getHandCopy())
			 {
				 List<Card> cards = inner.get(card.getSuit());
				 if (cards == null)
				 {
					 cards = new ArrayList<Card>();
					 inner.put(card.getSuit(), cards);
				 }
				 cards.add(card);
			 }
			 result.put(sp, inner);
		 }
		 return result;
	 }
	 private void doSimpleBid(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("simple bid: dealer = {}, tableId = {}",dealer,tableId);
		 SeatPosition position = dealer;
		 TestUtils.bid(ValidBidOption.PASS, tableId, position, stompSession);
		 TestUtils.bid(ValidBidOption.ONE_CLUBS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);

		 List<Map<String,Object>> messages = TestUtils.queueToList(messageQueue);
		 boolean redealMessage = false;
		 for (Map<String,Object> map:messages)
		 {
			 if(((String) map.get("message")).contains("the contract has been made")) redealMessage = true;
		 }
		 assertTrue(redealMessage);
		 logger.info("messages = {}", messages);
		 Table table = tableService.findById(tableId);
		 assertEquals(SeatPosition.nextPlayer(dealer), table.getCurrentContract().getDeclarer().getPosition());
		 assertEquals(1, table.getCurrentContract().getLevel());
		 assertEquals(BidSuit.CLUBS, table.getCurrentContract().getSuit());
	 }
	 private SeatPosition sitDown(Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
			stompSession.subscribe("/topic/table/"+tableId, new QueueStompFrameHandler());
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put("playerName", "NPlay");
			attributes.put("tableId", String.valueOf(tableId));
			attributes.put("position", "NORTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes.put("playerName", "SPlay");
			attributes.put("position", "SOUTH");
			stompSession.send("/app/table/sitDown", attributes);
			attributes.put("playerName", "EPlay");
			attributes.put("position", "EAST");
			stompSession.send("/app/table/sitDown", attributes);
			attributes.put("playerName", "WPlay");
			attributes.put("position", "WEST");
			stompSession.send("/app/table/sitDown", attributes);
			
			List<Map<String,Object>> messages = TestUtils.queueToList(messageQueue);
			logger.info("sat daown 4 players at one table, here are the messages: {}",messages);
			assertEquals(4,tableService.findById(tableId).playersSitting());
			int dealerSelectedMessageCount = 0;
			SeatPosition dealer = null;
			for (Map map:messages)
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
	 private Long enterRoomAndCreateTable(StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {			
			stompSession.subscribe("/topic/room", new QueueStompFrameHandler());
			
			stompSession.send("/app/room/enter", "NPlay");
			stompSession.send("/app/room/enter", "SPlay");
			stompSession.send("/app/room/enter", "EPlay");
			stompSession.send("/app/room/enter", "WPlay");
						
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