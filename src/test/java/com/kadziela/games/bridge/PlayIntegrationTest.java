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
	
	 @Autowired private TableService tableService;
	 @Autowired private QueueStompFrameHandler queueStompFrameHandler; 
	 @Autowired private ErrorStompFrameHandler errorStompFrameHandler; 
	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
		 StompSession stompSession = TestUtils.setupTest(URL, port,errorStompFrameHandler,queueStompFrameHandler);	
		 Long tableId = TestUtils.put4PlayersInRoomAndTable(stompSession, errorStompFrameHandler, queueStompFrameHandler, tableService, "NPlay","Splay","EPlay","WPlay");
		 Table table = tableService.findById(tableId);
		 assertNotNull(table);
		 doSimpleBid(table.getCurrentDealer().getPosition(), tableId, stompSession);
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

		 List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
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
}