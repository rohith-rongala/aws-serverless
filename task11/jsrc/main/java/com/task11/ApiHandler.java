package com.task11;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.handler.ReservationsHandler;
import com.task11.handler.TablesHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;

import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String USER_POOL_NAME = "cmtr-aa756657-simple-booking-userpool-test";
	private final CognitoIdentityProviderClient cognitoClient;
	private static final String USER_POOL_ID = getUserPoolId();
	private static final String APP_CLIENT_ID = getClientId();

	public ApiHandler() {
		cognitoClient = CognitoIdentityProviderClient.create();
	}

	public static String getUserPoolId() {

		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();

		ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().maxResults(10).build();
		ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);

		String userPoolId = listUserPoolsResponse.userPools().get(0).id();

		for(UserPoolDescriptionType userPool : listUserPoolsResponse.userPools()) {
			if (userPool.name().equals(USER_POOL_NAME)) {
				userPoolId = userPool.id();
				System.out.println("User Pool Id is: " + userPoolId);
				break;
			}
		}
		return userPoolId;
	}


	public static String getClientId() {

			CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();

			ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
					.userPoolId(USER_POOL_ID).maxResults(10).build();
			ListUserPoolClientsResponse listUserPoolClientsResponse = cognitoClient
					.listUserPoolClients(listUserPoolClientsRequest);

			String clientId = "";
			for (UserPoolClientDescription userPoolClient : listUserPoolClientsResponse.userPoolClients()) {
				if (userPoolClient.clientName().equals("client-app")) {
					clientId = userPoolClient.clientId();
					System.out.println("Client Id is: " + clientId);
					break;
				}
			}
			return clientId;
		}


	public  APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		Map<String,String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Accept-Version", "*");
		try {
			System.out.println(event);
			System.out.println(event.getResource());
			switch (event.getResource()) {
				case "/signup":
					return handleSignUp(event);
				case "/signin":
					return handleSignIn(event);
				case "/tables":
					return handleTables(event);
				case "/tables/{tableId}":
					return handleTables(event);
				case "/reservations":
					return handleReservations(event);
				default:
					break;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(headers);
	}

	private APIGatewayProxyResponseEvent handleReservations(APIGatewayProxyRequestEvent event) {
		Map<String,String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Accept-Version", "*");
		if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new ReservationsHandler().getAllReservations();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new ReservationsHandler().postReservations(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(headers);
	}

	private APIGatewayProxyResponseEvent handleTables(APIGatewayProxyRequestEvent event) {
		Map<String,String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Accept-Version", "*");
		System.out.println("Checking handle tables");
		System.out.println("Event : "+event);
		System.out.println("Event pathparamenter : "+event.getPathParameters());
		if(Objects.nonNull(event.getPathParameters())){
			System.out.println(event.getPathParameters().get("tableId")+"Entering ID tables");
			return new TablesHandler().getTables(event);
		} else if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new TablesHandler().getAllTables();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new TablesHandler().postTables(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(headers);
	}

	public APIGatewayProxyResponseEvent handleSignUp(APIGatewayProxyRequestEvent event) {
		System.out.println("Entering try");
		Map<String,String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Accept-Version", "*");
		try {
			System.out.println("Parsing request");
			Map<String, String> input = new Gson().fromJson(event.getBody(), HashMap.class);
			System.out.println("request body is parsed");
			String firstName = input.get("firstName");
			String lastName = input.get("lastName");
			String email = input.get("email");
			String password = input.get("password");

			// Validate the inputs here! (Check the email format and password rules)
			System.out.println("Creating user_request");

			AdminCreateUserRequest user_request = AdminCreateUserRequest.builder()
					.userPoolId(USER_POOL_ID)
					.username(email)
//					.userAttributes(
//							AttributeType.builder()
//									.name("firstName")
//									.value(firstName)
//									.build())
////							AttributeType.builder()
////									.name("lastName")
////									.value(lastName)
////									.build())

					.messageAction("SUPPRESS")
					.build();

			System.out.println(System.getenv("AWS_ACCESS_KEY_ID"));
			System.out.println("Cognito-Client creating user_request");
			cognitoClient.adminCreateUser(user_request);

			System.out.println("Setting password");
			// Setting password permanently
			AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
					.username(email)
					.password(password)
					.userPoolId(USER_POOL_ID)
					.permanent(true)
					.build();

			cognitoClient.adminSetUserPassword(passwordRequest);

			System.out.println("Returning");
			return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(headers);

		} catch (Exception e) {
			System.out.println(e);
			return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Failed to sign up!").withHeaders(headers);
		}
	}

	public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
		Map<String,String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "*");
		headers.put("Accept-Version", "*");
		Map<String, String> input = new Gson().fromJson(request.getBody(), HashMap.class);

		System.out.println("Handling sign in");

		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().region(Region.EU_CENTRAL_1).build();

		Map<String,String> authParameters = new HashMap<>();
		authParameters.put("USERNAME", input.get("email"));
		authParameters.put("PASSWORD", input.get("password"));
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		System.out.println(authParameters);
		System.out.println("Preparing Authrequest");
		InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
				.authFlow(AuthFlowType.USER_PASSWORD_AUTH)
				.authParameters(authParameters)
				.clientId(APP_CLIENT_ID)
				.build();

		try {
			System.out.println("Initiating Authrequest");
			System.out.println(authRequest);

			InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
			System.out.println(authResponse);

			response.setBody("{ \"accessToken\": \"" + authResponse.authenticationResult().accessToken() + "\"}");
			response.setStatusCode(200);

		} catch (NotAuthorizedException e) {
			System.out.println(e);
			response.setStatusCode(400);

		} catch (UserNotFoundException e) {
			System.out.println(e);
			response.setStatusCode(400);

		} catch (Exception e) {
			System.out.println(e);
			response.setStatusCode(400);

		}
		System.out.println("Returning "+response);
		response.setHeaders(headers);
		return response;
	}
}