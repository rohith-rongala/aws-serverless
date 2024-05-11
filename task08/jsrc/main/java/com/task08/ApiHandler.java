package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		layers = {"sdk_layer"},
		runtime = DeploymentRuntime.JAVA11
)

@LambdaUrlConfig(authType = AuthType.NONE,invokeMode = InvokeMode.BUFFERED)
@LambdaLayer(layerName = "sdk_layer",
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP,
		libraries = {"lib/task08-1.0.0.jar"}
)
public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		OpenMeteoClient openMeteo = new OpenMeteoClient();
		CompletableFuture<String> weather = openMeteo.getWeather();

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", weather.join());
		return resultMap;
	}
}
