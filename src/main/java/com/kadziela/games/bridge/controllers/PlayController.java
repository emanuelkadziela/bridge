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
import com.kadziela.games.bridge.service.RoomService;
import com.kadziela.games.bridge.util.MapUtil;

@Controller
public class PlayController 
{
	private static final Logger logger = LogManager.getLogger(PlayController.class);

	@Autowired private SimpMessageSendingOperations messagingTemplate;
	
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
			
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error("IllegalArgumentExceptiom occurred while trying to play a card ", iae);
	    	Map<String,String> errorMap = MapUtil.mappifyStringMessage("IllegalArgumentExceptiom occurred while trying to play a card ");
	    	errorMap.put("error",iae.getMessage());
	    	messagingTemplate.convertAndSend("/topic/errors",errorMap);	    	
	    }	    
	}
}