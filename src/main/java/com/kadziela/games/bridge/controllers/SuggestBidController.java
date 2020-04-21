package com.kadziela.games.bridge.controllers;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.kadziela.games.bridge.model.Card;
import com.kadziela.games.bridge.util.HandUtils;
import com.kadziela.games.bridge.util.MapUtil;

@Controller
public class SuggestBidController 
{
	private static final Logger logger = LogManager.getLogger(SuggestBidController.class);

	
	@MessageMapping("/bid/suggest")
	@SendTo("/topic/bid/suggest")
	public Map<String,Object> suggest(List<String> symbols)
	{
		logger.debug(String.format("Suggest Bid Controller received the request with this hand %s", symbols));		
    	return MapUtil.mappifyMessage(HandUtils.suggestFirstOpeningBid5CMBM(symbols)); 
	}
}