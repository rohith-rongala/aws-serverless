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
import com.task11.util.Constants;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;

import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.task11.util.Constants.APP_CLIENT_ID;
import static com.task11.util.Constants.USER_POOL_ID;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final CognitoIdentityProviderClient cognitoClient;
	private Constants constants = new Constants();

	public ApiHandler() {
		cognitoClient = CognitoIdentityProviderClient.create();
	}

	public  APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
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
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(constants.getHeaders());
	}

	private APIGatewayProxyResponseEvent handleReservations(APIGatewayProxyRequestEvent event) {
		if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new ReservationsHandler().getAllReservations();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new ReservationsHandler().postReservations(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(constants.getHeaders());
	}

	private APIGatewayProxyResponseEvent handleTables(APIGatewayProxyRequestEvent event) {
		if(Objects.nonNull(event.getPathParameters())){
			System.out.println(event.getPathParameters().get("tableId")+"Entering ID tables");
			return new TablesHandler().getTables(event);
		} else if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new TablesHandler().getAllTables();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new TablesHandler().postTables(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(constants.getHeaders());
	}

	public APIGatewayProxyResponseEvent handleSignUp(APIGatewayProxyRequestEvent event) {
		try {
			Map<String, String> input = new Gson().fromJson(event.getBody(), HashMap.class);
			String firstName = input.get("firstName");
			String lastName = input.get("lastName");
			String email = input.get("email");
			String password = input.get("password");

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
			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
			Map<String,String> headers = constants.getHeaders();
			response.setHeaders(headers);
			response.setStatusCode(200);
			return response;

		} catch (Exception e) {
			System.out.println(e);
			return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(constants.getHeaders());
		}
	}

	public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
		Map<String, String> input = new Gson().fromJson(request.getBody(), HashMap.class);

		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().region(Region.EU_CENTRAL_1).build();

		Map<String,String> authParameters = new HashMap<>();
		authParameters.put("USERNAME", input.get("email"));
		authParameters.put("PASSWORD", input.get("password"));
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		System.out.println("Preparing Authrequest");
		InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
				.authFlow(AuthFlowType.USER_PASSWORD_AUTH)
				.authParameters(authParameters)
				.clientId(APP_CLIENT_ID)
				.build();

		try {
			System.out.println("Initiating Authrequest");
			InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
			response.setBody("{ \"accessToken\": \"" + authResponse.authenticationResult().accessToken() + "\"}");
			response.setStatusCode(200);

		} catch (NotAuthorizedException e) {
			System.out.println(e);
			response.setStatusCode(400);
			response.setBody("Not Authorized");

		} catch (UserNotFoundException e) {
			System.out.println(e);
			response.setStatusCode(400);
			response.setBody("User not found");

		} catch (Exception e) {
			System.out.println(e);
			response.setStatusCode(400);
			response.setBody("Failed to initiate Authrequest");

		}
		Map<String,String> headers = constants.getHeaders();
		response.setHeaders(headers);
		return response;
	}
}