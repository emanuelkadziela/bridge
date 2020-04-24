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
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.BidSuit;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.CardUtils;
import com.kadziela.games.bridge.util.HandUtils;
import com.kadziela.games.bridge.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BiddingIntegrationTest 
{
	private static final Logger logger = LogManager.getLogger(BiddingIntegrationTest.class);
	
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	
	 @Autowired TableService tableService;
	 @Autowired private QueueStompFrameHandler queueStompFrameHandler; 
	 @Autowired private ErrorStompFrameHandler errorStompFrameHandler; 

	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {

		 StompSession stompSession = TestUtils.setupTest(URL, port,errorStompFrameHandler,queueStompFrameHandler);	
		 Long tableId = TestUtils.put4PlayersInRoomAndTable(stompSession, errorStompFrameHandler, queueStompFrameHandler, tableService, "NBid","SBid","EBid","WBid");
		 Table table = tableService.findById(tableId);
		 assertNotNull(table);
		 doBidding(table.getCurrentDealer().getPosition(), tableId, stompSession);
	 }
	 private void doBidding(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("bidding test: dealer = {}, tableId = {}",dealer,tableId);
		 doRedeal(dealer, tableId, stompSession);
		 doSimpleBid(dealer, tableId, stompSession);
		 doComplexBid(dealer, tableId, stompSession);
		 doSuggestedBid(dealer, tableId, stompSession);
	 }
	 private void doSuggestedBid(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("testing suggesed bid: dealer = {}, tableId = {}",dealer,tableId);
		 Table table = tableService.findById(tableId);
		 table.cleanupAfterPlay();
		 table.setCurrentDealer(table.getPlayerAtPosition(dealer));
		 table.deal();
		 SeatPosition position = dealer;
		 List<String> hand = CardUtils.convertFromEnumsToStrings(table.getPlayerAtPosition(position).getHandCopy());
		 int consecutivePasses = 0;
		 boolean expectRedeal = true;
		 while(consecutivePasses < 4)
		 {
			 logger.info("bidding with this hand: {} ",hand);
			 ValidBidOption suggestedBid = HandUtils.suggestFirstOpeningBid5CMBM(hand);
			 logger.info("suggested bid: {} ",suggestedBid);
			 TestUtils.bid(suggestedBid, tableId, position, stompSession);
			 if (!suggestedBid.equals(ValidBidOption.PASS))
			 {
				 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
				 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
				 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
				 consecutivePasses = 10;
				 expectRedeal = false;
			 }
			 consecutivePasses++;
			 position = SeatPosition.nextPlayer(position);
			 hand = CardUtils.convertFromEnumsToStrings(table.getPlayerAtPosition(position).getHandCopy());			 
		 }
		 List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
		 boolean redealMessage = false;
		 boolean contractMessage = false;
		 for (Map<String,Object> map:messages)
		 {
			 if(((String) map.get("message")).contains("redeal")) redealMessage = true;
			 if(((String) map.get("message")).contains("the contract has been made")) contractMessage = true;
		 }
		 if (expectRedeal) assertTrue(redealMessage);
		 else assertTrue(contractMessage);
	 }
	 private void doComplexBid(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("testing simple bid: dealer = {}, tableId = {}",dealer,tableId);
		 Table table = tableService.findById(tableId);
		 table.cleanupAfterPlay();
		 table.setCurrentDealer(table.getPlayerAtPosition(dealer));
		 SeatPosition position = dealer;
		 TestUtils.bid(ValidBidOption.PASS, tableId, position, stompSession);
		 TestUtils.bid(ValidBidOption.ONE_CLUBS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.ONE_DIAMONDS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.TWO_CLUBS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.TWO_NO_TRUMP, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.DOUBLE, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.REDOUBLE, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.THREE_CLUBS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.DOUBLE, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.REDOUBLE, tableId, position = SeatPosition.nextPlayer(position), stompSession);
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
		 logger.info("contract = {}", table.getCurrentContract());		 
		 assertEquals(SeatPosition.nextPlayer(dealer), table.getCurrentContract().getDeclarer().getPosition());
		 assertEquals(3, table.getCurrentContract().getLevel());
		 assertEquals(BidSuit.CLUBS, table.getCurrentContract().getSuit());
		 assertTrue(table.getCurrentContract().isRedoubled());
	 }
	 private void doSimpleBid(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("testing simple bid: dealer = {}, tableId = {}",dealer,tableId);
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
	 private void doRedeal(SeatPosition dealer,Long tableId, StompSession stompSession) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	 {
		 logger.info("testing four passes for redeal: dealer = {}, tableId = {}",dealer,tableId);
		 
		 logger.info("checking hands before four passes/redeal");
		 checkHands(tableId);
		 
		 SeatPosition position = dealer;
		 TestUtils.bid(ValidBidOption.PASS, tableId, position, stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 TestUtils.bid(ValidBidOption.PASS, tableId, position = SeatPosition.nextPlayer(position), stompSession);
		 
		 List<Map<String,Object>> messages = queueStompFrameHandler.getMessages();
		 logger.info("messages = {}", messages);
		 boolean redealMessage = false;
		 for (Map<String,Object> map:messages)
		 {
			 if(((String) map.get("message")).contains("redeal")) redealMessage = true;
		 }
		 assertTrue(redealMessage);
		 logger.info("checking hands after four passes/redeal");
		 checkHands(tableId);		 
	 }
	 private void checkHands(Long tableId)
	 {
		 Table table = tableService.findById(tableId);
		 for (SeatedPlayer player: table.getAllSeatedPlayers())
		 {
			 logger.info("position = {}, cards = {}", player.getPosition(),player.getHandCopy());
		 }
	 }
}