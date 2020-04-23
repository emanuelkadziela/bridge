package com.kadziela.games.bridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;

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
	 public static final void roomEnterPlayer(String name, StompSession stompSession,QueueStompFrameHandler queueStompFrameHandler) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
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
	 }
}