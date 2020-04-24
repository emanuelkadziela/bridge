package com.kadziela.games.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.kadziela.games.bridge.model.Contract;
import com.kadziela.games.bridge.model.Message;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.CardUtils;
import com.kadziela.games.bridge.util.HandUtils;
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
		 testWholeGame(table, stompSession);
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
	 private void testWholeGame(Table table,StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 table.cleanupAfterPlay();
		 dealCards(table);
		 table.setCurrentDealer(table.getPlayerAtPosition(SeatPosition.NORTH));
		 bid1Clubs(table.getId(), stompSession);
		 Contract contract = table.getCurrentContract();
		 assertNotNull(contract);
		 assertEquals(SeatPosition.NORTH,contract.getDeclarer().getPosition());
		 assertEquals(contract.getSuit(), BidSuit.CLUBS);
		 playCards(table, stompSession);
	 }
	 private void dealCards(Table table)
	 {
		 SeatedPlayer north = table.getPlayerAtPosition(SeatPosition.NORTH);
		 north.takeNewCards(CardUtils.convertFromStringsToEnums(Arrays.asList("2C","3C","4C","5C","6C","7C","8C","9C","10C","JC","QC","KC","AC")));
		 SeatedPlayer south = table.getPlayerAtPosition(SeatPosition.SOUTH);
		 south.takeNewCards(CardUtils.convertFromStringsToEnums(Arrays.asList("2D","3D","4D","5D","6D","7D","8D","9D","10D","JD","QD","KD","AD")));
		 SeatedPlayer east = table.getPlayerAtPosition(SeatPosition.EAST);
		 east.takeNewCards(CardUtils.convertFromStringsToEnums(Arrays.asList("2H","3H","4H","5H","6H","7H","8H","9H","10H","JH","QH","KH","AH")));
		 SeatedPlayer west = table.getPlayerAtPosition(SeatPosition.WEST);
		 west.takeNewCards(CardUtils.convertFromStringsToEnums(Arrays.asList("2S","3S","4S","5S","6S","7S","8S","9S","10S","JS","QS","KS","AS")));		 
	 }
	 private void bid1Clubs(Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 TestUtils.bid(ValidBidOption.FIVE_CLUBS, tableId, SeatPosition.NORTH, stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, SeatPosition.EAST, stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, SeatPosition.SOUTH, stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, SeatPosition.WEST, stompSession);		 
	 }
	 private void playCards(Table table, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 List<Card> north = new ArrayList<>(table.getPlayerAtPosition(SeatPosition.NORTH).getHandCopy());
		 List<Card> south = new ArrayList<>(table.getPlayerAtPosition(SeatPosition.SOUTH).getHandCopy());
		 List<Card> east = new ArrayList<>(table.getPlayerAtPosition(SeatPosition.EAST).getHandCopy());
		 List<Card> west = new ArrayList<>(table.getPlayerAtPosition(SeatPosition.WEST).getHandCopy());
		 
		 TestUtils.playCard(east.get(0), table.getId(), SeatPosition.EAST, stompSession);
		 TestUtils.playCard(south.get(0), table.getId(), SeatPosition.SOUTH, stompSession);
		 TestUtils.playCard(west.get(0), table.getId(), SeatPosition.WEST, stompSession);
		 TestUtils.playCard(north.get(0), table.getId(), SeatPosition.NORTH, stompSession);			 
		 
		 for (int i = 1; i < 13; i++)
		 {
			 TestUtils.playCard(north.get(i), table.getId(), SeatPosition.NORTH, stompSession);			 
			 TestUtils.playCard(east.get(i), table.getId(), SeatPosition.EAST, stompSession);
			 TestUtils.playCard(south.get(i), table.getId(), SeatPosition.SOUTH, stompSession);
			 TestUtils.playCard(west.get(i), table.getId(), SeatPosition.WEST, stompSession);
		 }
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
}