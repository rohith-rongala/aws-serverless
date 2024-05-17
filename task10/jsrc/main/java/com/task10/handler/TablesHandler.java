package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.pojo.Tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.amazonaws.services.dynamodbv2.document.*;


public class TablesHandler {

    private static AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();
    private static DynamoDB dynamoDB = new DynamoDB(ddbClient);

    // Initialize Gson object
    private static Gson gson = new Gson();

    private final String tableName = "cmtr-aa756657-Tables-test";

    public APIGatewayProxyResponseEvent getAllTables() {
//        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName);

        ScanResult result = ddbClient.scan(scanRequest);
        List<Tables> tables = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()) {
            Tables table = new Tables(); // assuming you have a POJO class 'Table' with these fields and their setters & getters
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

        return response;
    }

    public APIGatewayProxyResponseEvent postTables(APIGatewayProxyRequestEvent request) {
        System.out.println("Post Tables");
//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        System.out.println("Mapper created successfully");


        Tables tableItem = gson.fromJson(request.getBody(), Tables.class);
        System.out.println(tableItem.toString());
        System.out.println("Saving Tables");
        mapper.save(tableItem);

        System.out.println("Returning");
        // Construct response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("{\"id\": " + tableItem.getId() + "}");

        return response;
    }

    public APIGatewayProxyResponseEvent getTables(APIGatewayProxyRequestEvent event) {
        int tableId = Integer.parseInt(event.getPathParameters().get("tableId"));
        System.out.println(tableId);
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            // Fetch table item from DynamoDB
            Table table = dynamoDB.getTable(tableName); // replace with name of your DynamoDB table
            Item item = table.getItem("id", tableId); // Presuming primary key attribute name 'Id' in table

            System.out.println(item);
            // If table item with given ID does not exist, return 400
            if (item == null) {
                System.out.println("Item null");
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


            System.out.println("All set Response");
            response.setStatusCode(200);
            response.setBody(gson.toJson(tables));

        } catch (Exception e) {
            response.setStatusCode(400);
            System.out.println(e);
//            response.setBody(e.getMessage());
        }

        return response;
    }


}



