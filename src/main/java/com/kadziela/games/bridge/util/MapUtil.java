package com.kadziela.games.bridge.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil 
{
	public static Map<String, Object> mappifyMessage(Object message)
	{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message", message);
		return map;
	}
	public static Map<String, Object> mappifyMessage(String key, Object message)
	{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(key, message);
		return map;
	}
}