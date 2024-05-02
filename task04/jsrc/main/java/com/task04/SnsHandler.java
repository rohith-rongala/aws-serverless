package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RegionScope;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEventSource(regionScope = RegionScope.DEFAULT,targetTopic = "lambda_topic")
public class SnsHandler implements RequestHandler<SNSEvent, String> {

	public String handleRequest(SNSEvent event, Context context) {
		for (SNSEvent.SNSRecord message : event.getRecords()) {
			String messageBody = message.toString();
			System.out.println("Received record: " + messageBody);
		}
		return "Executed successfully from lambda sns handlers";
	}
}
