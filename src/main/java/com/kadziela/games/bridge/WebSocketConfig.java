package com.kadziela.games.bridge;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
@ComponentScan("com.kadziela.games.bridge")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer 
{
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic/", "/queue/");		
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/kadziela-bridge-websocket").setAllowedOrigins("*").withSockJS();
	}

//	@Override 
//	public void registerStompEndpoints(StompEndpointRegistry registry) 
//	{
//		registry.addEndpoint("/kadziela-bridge-websocket").setHandshakeHandler(new DefaultHandshakeHandler() 
//		{
//			public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,Map attributes) throws Exception 
//			{
//		        if (request instanceof ServletServerHttpRequest) 
//		        {
//		            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//		            HttpSession session = servletRequest.getServletRequest().getSession();
//		            attributes.put("sessionId", session.getId());
//	            }
//            return true;
//			}
//		}).withSockJS();
//	}
}