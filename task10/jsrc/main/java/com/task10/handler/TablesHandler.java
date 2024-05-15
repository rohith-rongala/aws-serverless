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

public class TablesHandler {
    public APIGatewayProxyResponseEvent getAllTables() {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        String tableName = "cmtr-aa756657-Tables-test";

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName);

        ScanResult result = ddb.scan(scanRequest);
        List<Tables> tables = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()){
            Tables table = new Tables(); // assuming you have a POJO class 'Table' with these fields and their setters & getters
            table.setId(Integer.parseInt(item.get("id").getN()));
            table.setNumber(Integer.parseInt(item.get("number").getN()));
            table.setPlaces(Integer.parseInt(item.get("places").getN()));
            table.setVip(Boolean.parseBoolean(String.valueOf(item.get("isVip").getBOOL())));

            if (item.containsKey("minOrder")) {
                table.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
            }
            tables.add(table);
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(new Gson().toJson(tables));

        return response;
    }

    public APIGatewayProxyResponseEvent postTables(APIGatewayProxyRequestEvent request) {
        System.out.println("Post Tables");
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

//        String tableName = "cmtr-aa756657-Tables";
        System.out.println("Mapper created successfully");

        Gson gson = new Gson();
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
}
