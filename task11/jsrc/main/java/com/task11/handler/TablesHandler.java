package com.task11.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task11.pojo.Tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.amazonaws.services.dynamodbv2.document.*;
import com.task11.util.Constants;


public class TablesHandler {

    private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private static DynamoDB dynamoDB = new DynamoDB(client);
    private static Gson gson = new Gson();
    Map<String, String> headers = new Constants().getHeaders();

    public APIGatewayProxyResponseEvent getAllTables() {
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(Constants.TABLES_NAME);

        ScanResult result = client.scan(scanRequest);
        List<Tables> tables = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()) {
            Tables table = new Tables();
            table.setId(Integer.parseInt(item.get("id").getN()));
            table.setNumber(Integer.parseInt(item.get("number").getN()));
            table.setPlaces(Integer.parseInt(item.get("places").getN()));
            table.setVip(Boolean.parseBoolean(String.valueOf(item.get("vip"))));

            if (item.containsKey("minOrder")) {
                table.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
            }
            tables.add(table);
        }

        Map<String, List<Tables>> tablesMap = new HashMap<>();
        tablesMap.put("tables", tables);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(gson.toJson(tablesMap));
        response.setHeaders(headers);

        return response;
    }

    public APIGatewayProxyResponseEvent postTables(APIGatewayProxyRequestEvent request) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Tables tableItem = gson.fromJson(request.getBody(), Tables.class);
        System.out.println("Saving Tables");
        mapper.save(tableItem);

        // Construct response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("{\"id\": " + tableItem.getId() + "}");
        response.setHeaders(headers);
        return response;
    }

    public APIGatewayProxyResponseEvent getTables(APIGatewayProxyRequestEvent event) {
        int tableId = Integer.parseInt(event.getPathParameters().get("tableId"));
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            // Fetch table item from DynamoDB
            Table table = dynamoDB.getTable(Constants.TABLES_NAME);
            Item item = table.getItem("id", tableId);

            if (item == null) {
                response.setStatusCode(400);
                return response;
            }

            Tables tables = new Tables();
            tables.setId(Integer.parseInt(item.getString("id")));
            tables.setNumber(Integer.parseInt(item.getString("number")));
            tables.setPlaces(Integer.parseInt(item.getString("places")));
            tables.setVip(Boolean.parseBoolean(String.valueOf(item.get("vip"))));
            if (item.isPresent("minOrder"))
                tables.setMinOrder(Integer.parseInt(item.getString("minOrder")));

            response.setStatusCode(200);
            response.setBody(gson.toJson(tables));

        } catch (Exception e) {
            System.out.println(e);
            response.setStatusCode(400);
            response.setBody("Table with id " + tableId + " not found");
        }
        response.setHeaders(headers);
        return response;
    }

}



