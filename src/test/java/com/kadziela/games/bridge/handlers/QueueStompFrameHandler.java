package com.kadziela.games.bridge.handlers;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Service;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class QueueStompFrameHandler implements StompFrameHandler
{
	private static final Logger logger = LogManager.getLogger(QueueStompFrameHandler.class);
 	private final BlockingQueue<Map<String,Object>> messageQueue = new LinkedBlockingQueue<Map<String,Object>>();
 	
	@Override
	public Type getPayloadType(StompHeaders stompHeaders) 
	{
	    logger.info("stompHeaders: {}",stompHeaders.toString());
	    return Map.class;
	}
	@Override
	public void handleFrame(StompHeaders stompHeaders, Object o)
	{
		logger.info("handleFrame message payload: {} ",(Map<String, Object>) o);
		try 
		{
			messageQueue.offer((Map<String, Object>) o, 5, TimeUnit.SECONDS);			
		}
		catch(InterruptedException ie) 
		{
			logger.error(ie);
		}
	}
	public List<Map<String,Object>> getMessages() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
	{
		List<Map<String,Object>> messages = new ArrayList<Map<String,Object>>();
		while (true)
		{
			Map<String,Object> map2 = messageQueue.poll(5, TimeUnit.SECONDS);
			if (map2 != null) 
			{
				messages.add(map2);
			}
			else break;
		}
		return messages;
	}
}