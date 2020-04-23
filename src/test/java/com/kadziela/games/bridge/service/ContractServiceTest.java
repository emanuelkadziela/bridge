package com.kadziela.games.bridge.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.model.Player;
import com.kadziela.games.bridge.model.Table;
import com.kadziela.games.bridge.model.enumeration.SeatPosition;

@SpringBootTest
public class ContractServiceTest 
{
	@Autowired RoomService roomService;
	@Autowired TableService tableService;
	@Autowired ContractService contractService;
	
	@Test
	void testScore()
	{
		Table table = tableService.create();
		Player north = roomService.addPlayer("northC");
		Player east = roomService.addPlayer("eastC");
		Player south = roomService.addPlayer("southC");
		Player west = roomService.addPlayer("westC");
		tableService.sitDown(north, table.getId(), SeatPosition.NORTH);
		tableService.sitDown(east, table.getId(), SeatPosition.EAST);
		tableService.sitDown(south, table.getId(), SeatPosition.SOUTH);
		tableService.sitDown(west, table.getId(), SeatPosition.WEST);		
		Map<Card,SeatPosition> map = tableService.chooseFirstDealer(table);		 
		assertNotNull(map);
		assertNotNull(table.getCurrentDealer());
	}
}