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
public class ErrorStompFrameHandler implements StompFrameHandler
{
	private static final Logger logger = LogManager.getLogger(ErrorStompFrameHandler.class);
	@Override public Type getPayloadType(StompHeaders stompHeaders) 
	{
	    logger.info(stompHeaders.toString());
	    return Map.class;
	}
	@Override public void handleFrame(StompHeaders stompHeaders, Object o) {logger.error((Map) o);}
 }