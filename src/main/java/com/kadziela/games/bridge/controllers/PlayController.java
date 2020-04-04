package com.kadziela.games.bridge.controllers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.service.RoomService;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.MapUtil;

@Controller
public class PlayController 
{
	private static final Logger logger = LogManager.getLogger(PlayController.class);

	@Autowired private SimpMessageSendingOperations messagingTemplate;
	@Autowired private TableService tableService;

	/**
	 * Plays a card from a player
	 * @param attributes the map of attributes needed to execute the call, must include the card (suit and rank) and the position which played it.
	 */
	@MessageMapping("/play/card")	
	public void playCard(Map<String,String> attributes)
	{
		logger.debug(String.format("Play Controller received the request to play a card with these attributes", attributes));
	    try
	    {	
			Assert.notNull(attributes, "the map of attributes cannot be null");
			String suit = attributes.get("suit");
			String rank = attributes.get("rank");
			String position = attributes.get("position");
			String tableId = attributes.get("tableId");
			Assert.notNull(tableId, "tableId must be passed into this method inside the attributes parameter");
			Assert.notNull(position, "position must be passed into this method inside the attributes parameter");
			Assert.notNull(rank, "rank must be passed into this method inside the attributes parameter");
			Assert.notNull(suit, "suit must be passed into this method inside the attributes parameter");
			Card card = new Card(Rank.valueOf(rank),Suit.valueOf(suit));
			SeatPosition sp = SeatPosition.valueOf(position);
			tableService.playCard(card, Long.valueOf(tableId), SeatPosition.valueOf(position));
			Map<String,Object> response = MapUtil.mappifyMessage(String.format("%s played %s",position,card));
			response.put("card", card);
			response.put("position", position);
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error("IllegalArgumentExceptiom occurred while trying to play a card ", iae);
	    	Map<String,Object> errorMap = MapUtil.mappifyMessage("IllegalArgumentExceptiom occurred while trying to play a card ");
	    	errorMap.put("error",iae.getMessage());
	    	messagingTemplate.convertAndSend("/topic/errors",errorMap);	    	
	    }	    
	    
	}
}