package com.kadziela.games.bridge.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import com.kadziela.games.bridge.service.RoomService;

@Controller
public class PlayController 
{
	private static final Logger logger = LogManager.getLogger(PlayController.class);

	@Autowired private RoomService roomService;
	@Autowired private SimpMessageSendingOperations messagingTemplate;
	
	@MessageMapping("/TBD")	
	public void TBD(String name)
	{
		logger.debug(String.format("Room Controller received the request for %s to enter the room", name));
	    try
	    {
			
		    messagingTemplate.convertAndSend("/queue/private/"+name, "Hello new player ");
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error("Unable to create a new player with username: "+ name+", because that name is already in use ", iae);
		    messagingTemplate.convertAndSend("/topic/errors","Unable to create a new player with username: "+ name+", because that name is already in use ");	    	
	    }	    
	}
}