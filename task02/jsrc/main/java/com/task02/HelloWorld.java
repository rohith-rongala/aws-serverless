package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;


import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@LambdaUrlConfig(invokeMode = InvokeMode.BUFFERED,authType = AuthType.NONE)
public class HelloWorld implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		System.out.println("Hello from lambda");
		Map<String, String> body = new HashMap<>();
		body.put("message", "Hello from Lambda");

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("headers", new HashMap<String, String>() {{
			put("Content-Type", "application/json");
		}});
		resultMap.put("body", new Gson().toJson(body));     // you might need to import com.google.gson.Gson;
		return resultMap;
	}
}
