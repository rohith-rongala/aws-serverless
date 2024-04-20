package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
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
	public Map<String, Object> handleRequest(Object input, Context context) {
		System.out.println("Handling Lambda request");
		Map<String,Object> responseBody = new HashMap<>();
		responseBody.put("statusCode", 200);
		responseBody.put( "message", "Hello from Lambda");
		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", 200);
		response.put("body", responseBody);

		return response;
	}
}
