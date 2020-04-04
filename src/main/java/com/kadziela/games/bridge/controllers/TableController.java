package com.kadziela.games.bridge.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.SeatedPlayer;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;
import com.kadziela.games.bridge.service.RoomService;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.MapUtil;

/**
 * @author Emanuel M. Kadziela
 *	A websocket controller for managing a contract bridge table.
 */
@Controller
public class TableController 
{
	private static final Logger logger = LogManager.getLogger(TableController.class);

	@Autowired private RoomService roomService;
	@Autowired private TableService tableService;
	@Autowired private SimpMessageSendingOperations messagingTemplate;
	
	/**
	 * Opens a new table
	 * @return a collection of the currently extant tables, on the /topic/room destination
	 */
	@MessageMapping("/table/openNew")
	@SendTo("/topic/room")
	public Long openNew()
	{
		logger.debug("opening a new table");
		Table table = tableService.create();
		return table.getId();
	}
	/**
	 * Returns a collection of all tables in the room, sending it to the /topic/room destination
	 * @return a collection of all tables in the room
	 */
	@MessageMapping("/table/getAll")
	@SendTo("/topic/room")
	public Collection<Table> getAll()
	{
		logger.debug("returning all tables");
		return tableService.getAllTables();
	}
	/**
	 * Given a name of a Player who is in the room, the id of a table in the room, and a position, this method will attempt to sit the player down at the
	 * given position at the given table, validating that there is no one already sitting in that seat. It will then publish a message to /queue/private/<playerName>,
	 * telling them they have been successfully sat down, and it will publish a similar message to the /topic/table/<tableId> destination.
	 * Any errors will result in messages published to the /topic/errors destination. 
	 * @param attributes - the map of attributes, must contain a player, a position and a table (at least). 
	 * The keys are as follows: playerName, tableId, position
	 */
	@MessageMapping("/table/sitDown")
	public void sitDown(Map<String,String> attributes)
	{
    	String playerName = null;
    	String tableId = null;
    	String position = null;
		logger.debug(String.format("Table Controller received a request to sit a player down at a table with the following attributes %s", attributes.toString()));
	    try
	    {
			Assert.notNull(attributes,"the input attributes canot be null");
	    	playerName = attributes.get("playerName");
	    	tableId = attributes.get("tableId");
	    	position = attributes.get("position");
	    	Assert.notNull(playerName, "the playerName must be passed in the attribute map");
	    	Assert.notNull(tableId, "the tableId must be passed in the attribute map");
	    	Assert.notNull(position, "the position must be passed in the attribute map");
	    	
	    	Player player = roomService.findByName(playerName);
	    	Assert.notNull(player,String.format("Player named %s is not in the room",playerName));
	    	Table table = tableService.sitDown(player, Long.valueOf(tableId), SeatPosition.valueOf(position));
	    	
	    	messagingTemplate.convertAndSend("/queue/private/"+playerName, MapUtil.mappifyStringMessage( 
    			String.format("you have successfully sat down at table %s in position %s",tableId,position)));

    		Map<String,String> response = MapUtil.mappifyStringMessage(String.format("player %s has successfully sat down at table %s in position %s",playerName,tableId,position));
    		response.put("playerName", playerName);
    		response.put("tableId", tableId);
    		response.put("position", position);    		
	    	messagingTemplate.convertAndSend("/topic/table/"+tableId, response);
	    	
	    	if(table.getAllSeatedPlayers().size() == 4)
	    	{
	    		logger.debug(String.format("4 players have sat down at table %s, so dealing will commence shortly ",tableId));
	    		deal(Long.valueOf(tableId));
	    	}
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error(String.format("An IllegalArgumentException occurred while trying to sit player %s at table %s and position %s ",playerName,tableId,position),iae);
		    messagingTemplate.convertAndSend("/topic/errors", MapUtil.mappifyStringMessage(
	    		String.format("An IllegalArgumentException occurred while trying to sit player %s at table %s and position %s. The error message is: %s",playerName,tableId,position,iae.getMessage())));	    	
	    }	    
	    catch (IllegalStateException ise)
	    {
	    	logger.error(String.format("An IllegalStateException occurred while trying to sit player %s at table %s and position %s ",playerName,tableId,position),ise);
		    messagingTemplate.convertAndSend("/topic/errors",MapUtil.mappifyStringMessage(
	    		String.format("An IllegalStateException occurred while trying to sit player %s at table %s and position %s. The error message is: %s",playerName,tableId,position,ise.getMessage())));	    	
	    }	    
	}
	/**
	 * given a name of a Player who is in the room, the id of a table in the room, and a position, this method will attempt to stand the player up from the
	 * given position at the given table, validating that there is in fact that player in that position 
	 * Any errors will result in messages published to the /topic/errors destination. 
	 * @param attributes - the map of attributes, must contain a player, a position and a table (at least). 
	 * The keys are as follows: playerName, tableId, position
	 */
	@MessageMapping("/table/standUp")
	public void standUp(Map<String,String> attributes)
	{
    	String playerName = null;
    	String tableId = null;
    	String position = null;
		logger.debug(String.format("Table Controller received a request to stand a player up with the following attributes %s", attributes.toString()));
	    try
	    {
			Assert.notNull(attributes,"the input attributes canot be null");
	    	playerName = attributes.get("playerName");
	    	tableId = attributes.get("tableId");
	    	position = attributes.get("playerName");
	    	Assert.notNull(playerName, "the playerName must be passed in the attribute map");
	    	Assert.notNull(tableId, "the tableId must be passed in the attribute map");
	    	Assert.notNull(position, "the position must be passed in the attribute map");
	    	
	    	Player player = roomService.findByName(playerName);
	    	Assert.notNull(player,String.format("Player named %s is not in the room",playerName));
	    	tableService.standUp(player, Long.valueOf(tableId), SeatPosition.valueOf(position));
	    	
	    	messagingTemplate.convertAndSend("/queue/private/"+playerName, MapUtil.mappifyStringMessage( 
    			String.format("you have successfully stood up from table %s and position %s",tableId,position)));
	    	messagingTemplate.convertAndSend("/topic/table/"+tableId, MapUtil.mappifyStringMessage( 
    			String.format("player %s has successfully stood up from table %s in position %s",playerName,tableId,position)));
	    }
	    catch (IllegalArgumentException iae)
	    {
	    	logger.error(String.format("An IllegalArgumentException occurred while trying to stand up a player %s at table %s and position %s ",playerName,tableId,position),iae);
		    messagingTemplate.convertAndSend("/topic/errors", MapUtil.mappifyStringMessage(
		    		String.format("An IllegalArgumentException occurred while trying to stand up a player %s at table %s and position %s. The error message is: %s",playerName,tableId,position,iae.getMessage())));	    	
	    }	    
	    catch (IllegalStateException ise)
	    {
	    	logger.error(String.format("An IllegalStateException occurred while trying to stand up a player %s at table %s and position %s ",playerName,tableId,position),ise);
		    messagingTemplate.convertAndSend("/topic/errors", MapUtil.mappifyStringMessage(
	    		String.format("An IllegalStateException occurred while trying to stand up a player %s at table %s and position %s. The error message is: %s",playerName,tableId,position,ise.getMessage())));	    	
	    }	    
	}
	/**
	 * Given the id of a table in the room, this method will validate that there are 4 players sitting and deal the cards. 
	 * The first dealer is selected by dealing a card face up to every player; the player with the highest card becomes the first dealer.
	 * Afterwards, the dealer moves clockwise around the table.
	 * Any errors will result in messages published to the /topic/errors destination. 
	 * @param tableId the id of the table where the dealing is to take place 
	 */
	@MessageMapping("/table/deal")
	public void deal(Long tableId)
	{
		logger.debug(String.format("Table Controller is about to deal cards at table %s", tableId));
		Table table = tableService.findById(tableId);
		try
		{
			Assert.notNull(table, String.format("Cannot find any existing tables for table id %s",tableId));
			if (table.getAllSeatedPlayers().size() < 4)
			{
		    	logger.error(String.format("There are only %s seated players at table with id %s, so cards will not be dealt ",table.getAllSeatedPlayers().size(),tableId));
			    messagingTemplate.convertAndSend("/topic/errors",
		    		String.format("There are only %s seated players at table with id %s, so cards will not be dealt ",table.getAllSeatedPlayers().size(),tableId));
			    return;
			}
			if (table.getCurrentDealer() == null)
			{
				logger.debug(String.format("dealing at table %s for the first time, choosing the first dealer ... ", table));
				Map<Card,SeatPosition> map = tableService.chooseFirstDealer(table);
		    	messagingTemplate.convertAndSend("/topic/table/"+tableId, map);
		    	messagingTemplate.convertAndSend("/topic/table/"+tableId, String.format("current dealer is %s ", table.getCurrentDealer().getPosition()));				
			}
			else
			{
				logger.debug(String.format("dealing at table %s ", table));
				tableService.deal(table);
				for(SeatedPlayer player : table.getAllSeatedPlayers())
				{
					messagingTemplate.convertAndSend("/queue/private/"+player.getPlayer().getName(), player.getHandCopy());
				}
			}
		}
		catch (IllegalArgumentException iae)
	    {
	    	logger.error(String.format("An IllegalArgumentException occurred while trying to deal cards at table with id %s ",tableId),iae);
		    messagingTemplate.convertAndSend("/topic/errors",
		    		String.format("An IllegalArgumentException occurred while trying to deal cards at table with id %s. The error message is: %s",tableId,iae.getMessage()));	    	
	    }	    	    
	}
}