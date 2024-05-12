package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
	roleName = "processor-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		tracingMode = TracingMode.Active
)

@LambdaUrlConfig(invokeMode = InvokeMode.BUFFERED,authType = AuthType.NONE)
public class Processor implements RequestHandler<Object, String> {

	private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private final DynamoDB dynamoDB = new DynamoDB(client);
	private final Table table = dynamoDB.getTable("cmtr-aa756657-Weather");
	private final Gson gson = new Gson();
	private final HttpClient httpClient = HttpClient.newHttpClient();


	public String handleRequest(Object input, Context context) {
		try {
			// Step 1: Call the weather API
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m"))
					.build();
			String responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

			// Step 2: Parse the weather API response
			//WeatherResponse weather = gson.fromJson(responseBody, WeatherResponse.class);

			// Step 3: Generate a UUID for the 'id' field
			String id = UUID.randomUUID().toString();


			// Step 4: Insert into DynamoDB

			Item eventItem = new Item()
					.withPrimaryKey("id", id)
					.withString("forecast", responseBody);

			// Save event data to DynamoDB
			table.putItem(eventItem);
			//table.putItem(Item.fromJSON(gson.toJson(new WeatherItem(id, weather))));

			return "Success";
		} catch (Exception e) {
			// Handle exception
			context.getLogger().log(e.getMessage());
			return "Failure";
		}
	}
}

/*class WeatherResponse {
	double latitude;
	double longitude;
	double elevation;
	double generationtime_ms;
	Hourly hourly;
	HourlyUnits hourly_units;
	String timezone;
	String timezone_abbreviation;
	double utc_offset_seconds;

	// getters and setters
	// latitude
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	// longitude
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	// other getters and setters...
}

class Hourly {
	double[] temperature_2m;
	String[] time;

	// getters and setters
}

class HourlyUnits {
	String temperature_2m;
	String time;

	// getters and setters
}*/



