package com.task11.util;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

public class Constants {


    public static final String USER_POOL_NAME = "cmtr-aa756657-simple-booking-userpool-test";
    public static final String TABLES_NAME = "cmtr-aa756657-Tables-test";
    public static final String RESERVATIONS_NAME = "cmtr-aa756657-Reservations-test";
    public static final String USER_POOL_ID = getUserPoolId();
	public static final String APP_CLIENT_ID = getClientId();
    private static CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();

    public Constants() {

    }

    public Map<String,String> getHeaders() {
        Map<String,String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "*");
        headers.put("Accept-Version", "*");
        return headers;
    }

    public static String getUserPoolId() {
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().maxResults(10).build();
        ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);

        String userPoolId = listUserPoolsResponse.userPools().get(0).id();

        for(UserPoolDescriptionType userPool : listUserPoolsResponse.userPools()) {
            if (userPool.name().equals(Constants.USER_POOL_NAME)) {
                userPoolId = userPool.id();
                System.out.println("User Pool Id is: " + userPoolId);
                break;
            }
        }
        return userPoolId;
    }


    public static String getClientId() {
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

}
