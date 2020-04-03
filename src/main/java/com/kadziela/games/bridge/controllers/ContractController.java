package com.kadziela.games.bridge.controllers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import com.kadziela.games.bridge.model.Bid;
import com.kadziela.games.bridge.model.Contract;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.model.enumeration.ValidBidOption;
import com.kadziela.games.bridge.service.ContractService;
import com.kadziela.games.bridge.service.TableService;

@Controller
public class ContractController 
{
	private static final Logger logger = LogManager.getLogger(ContractController.class);

	@Autowired private ContractService contractService;
	@Autowired private TableService tableService;
	@Autowired private SimpMessageSendingOperations messagingTemplate;

	/**
	 * Given a table id (Long) , a position (SeatPosition) and a bid (ValidBidOption), this method will verify that the bid is valid 
	 * (i.e. coming from the correct position and higher than the last one, etc.), store it in an internal
	 * map, and publish it on the /topic/table/<tableid> destination.  
	 * @param attributes
	 */
	@MessageMapping("/contract/bid")
	public void bid(Map<String,String> attributes)
	{
		logger.debug(String.format("Contract Controller received the request to bid with the following attributes %s", attributes));
    	String tableId = null;
    	String position = null;
    	String bid = null;
		logger.debug(String.format("Contract Controller received a bid request with the following attributes %s", attributes.toString()));
	    try
	    {
			Assert.notNull(attributes,"the input attributes canot be null");
			tableId = attributes.get("tableId");
			position = attributes.get("position");
			bid = attributes.get("bid");
			Assert.notNull(tableId, "tableId must be passed into this method inside the attributes parameter");
			Assert.notNull(position, "position must be passed into this method inside the attributes parameter");
			Assert.notNull(bid, "bid must be passed into this method inside the attributes parameter");
			Table table = tableService.findById(Long.valueOf(tableId));
			SeatPosition sp = SeatPosition.valueOf(position);
			ValidBidOption vbo = ValidBidOption.valueOf(bid);
			SeatedPlayer seatedPlayer = table.getPlayerAtPosition(sp);
			Bid b = contractService.bid(seatedPlayer, vbo, table);
			if (contractMade(table))
			{
				messagingTemplate.convertAndSend(String.format("/topic/table/%s",table.getId()),"The last bid was a third pass, the contract has been made");		    		
			}
			if (redeal(table))
			{
				messagingTemplate.convertAndSend(String.format("/topic/table/%s",table.getId()),"The last bid was a fourth pass, redeal");		    		
			}
			Contract contract = table.createNewContract();
			messagingTemplate.convertAndSend(String.format("/topic/table/%s",table.getId()),contract);		    					
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error(String.format("An IllegalArgumentException occurred while trying to bid %s at table %s and position %s ",bid,tableId,position),iae);
		    messagingTemplate.convertAndSend("/topic/errors",
		    		String.format("An IllegalArgumentException occurred while trying to bid %s at table %s and position %s. The error message is: %s",bid,tableId,position,iae.getMessage()));	    	
	    }	    
	    catch (IllegalStateException ise)
	    {
	    	logger.error(String.format("An IllegalStateException occurred while trying to bid %s at table %s and position %s ",bid,tableId,position),ise);
		    messagingTemplate.convertAndSend("/topic/errors",
	    		String.format("An IllegalStateException occurred while trying to bid %s at table %s and position %s. The error message is: %s",bid,tableId,position,ise.getMessage()));	    	
	    }	    

	}
	private boolean contractMade(Table table)
	{
		if (table.getCurrentBidOptions().size() > 2 &&
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-1).equals(ValidBidOption.PASS) && 
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-2).equals(ValidBidOption.PASS) && 
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-3).equals(ValidBidOption.PASS)) 		
			return true;		
		return false;
	}
	private boolean redeal(Table table)
	{
		if (table.getCurrentBidOptions().size() == 3 &&
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-1).equals(ValidBidOption.PASS) && 
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-2).equals(ValidBidOption.PASS) && 
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-3).equals(ValidBidOption.PASS) &&
			table.getCurrentBidOptions().get(table.getCurrentBidOptions().size()-4).equals(ValidBidOption.PASS)) 		
			return true;		
		return false;
	}
}