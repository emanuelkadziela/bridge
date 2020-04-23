package com.kadziela.games.bridge;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import com.kadziela.games.bridge.handlers.ErrorStompFrameHandler;
import com.kadziela.games.bridge.handlers.QueueStompFrameHandler;
import com.kadziela.games.bridge.service.TableService;
import com.kadziela.games.bridge.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnterRoomCreateTableSitDownNiceGPTest 
{
	 @Value("${local.server.port}")
	 private int port;
	 private String URL;
	 
	 @Autowired private TableService tableService;
	 @Autowired private QueueStompFrameHandler queueStompFrameHandler; 
	 @Autowired private ErrorStompFrameHandler errorStompFrameHandler; 
	 	 	 
	 @Test
	 public void testCreateGameEndpoint() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException 
	 {
		 TestUtils.put4PlayersInRoomAndTable(URL, port, errorStompFrameHandler, queueStompFrameHandler, tableService, "North","South","East","West");
	 }
}