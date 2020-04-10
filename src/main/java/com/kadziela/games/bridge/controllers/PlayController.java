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
import com.kadziela.games.bridge.model.ContractScore;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.Trick;
import com.kadziela.games.bridge.model.enumeration.Rank;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.Suit;
import com.kadziela.games.bridge.service.ContractService;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.MapUtil;

@Controller
public class PlayController 
{
	private static final Logger logger = LogManager.getLogger(PlayController.class);

	@Autowired private SimpMessageSendingOperations messagingTemplate;
	@Autowired private TableService tableService;
	@Autowired private ContractService contractService;

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
			Trick trick = tableService.playCard(card, Long.valueOf(tableId), SeatPosition.valueOf(position));
			Map<String,Object> response = MapUtil.mappifyMessage(String.format("%s played %s",position,card));
			response.put("card", card);
			response.put("position", position);
			response.put("nextPosition", SeatPosition.nextPlayer(SeatPosition.valueOf(position)));
			if (trick != null) 
			{
				response.put("trick", trick); 
				response.put("nextPosition", trick.getWinner().getPosition());
			}
			messagingTemplate.convertAndSend(String.format("/topic/table/%s",tableId),response);
			Table table = tableService.findById(Long.valueOf(tableId));
			if (table.getTricks().size() == 13)
			{
				logger.info(String.format("13 tricks collected, calculating the contract score"));
				Map<String,String> scoreBreakdown = contractService.calculateScore(table.getCurrentContract(), table.getTricks(), tableService.getPersistentHands(Long.valueOf(tableId)),table);
				messagingTemplate.convertAndSend(String.format("/topic/table/%s",tableId),MapUtil.mappifyMessage("score",scoreBreakdown));				
			}
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error("IllegalArgumentExceptiom occurred while trying to play a card ", iae);
	    	messagingTemplate.convertAndSend("/topic/errors",MapUtil.mappifyMessage("error",String.format("IllegalArgumentExceptiom occurred while trying to play a card %s",iae.getMessage())));	    	
	    }	   
	}	
}