package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.pojo.Reservations;

import com.amazonaws.services.dynamodbv2.document.*;
import com.task10.pojo.Tables;


import java.util.*;


public class ReservationsHandler {

    private final String ReservationsTable = "cmtr-aa756657-Reservations-test";
    private final String TablesTable = "cmtr-aa756657-Tables-test";

//    private static AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();
//    private static DynamoDB dynamoDB = new DynamoDB(ddbClient);

    public APIGatewayProxyResponseEvent postReservations(APIGatewayProxyRequestEvent request) {

        System.out.println("Post Reservations");
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        System.out.println("Mapper created successfully");

        Gson gson = new Gson();
        Reservations reservationItem = gson.fromJson(request.getBody(), Reservations.class);
        System.out.println("ReservationItem : "+reservationItem);

        System.out.println("Checking Table with "+reservationItem.getTableNumber());
        // Check if table exists
//        Table table = dynamoDB.getTable(TablesTable); // Replace with the name of your 'Tables' table
//        Item tableItem = table.getItem("id", reservationItem.getTableNumber());
//        System.out.println(tableItem);
//
         DynamoDBQueryExpression<Tables> queryExpression = new DynamoDBQueryExpression<Tables>()
                .withIndexName("TableIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("#num = :val")
                 .withExpressionAttributeNames(Collections.singletonMap("#num", "number"))
                 .withExpressionAttributeValues(Collections.singletonMap(":val", new AttributeValue().withN(String.valueOf(reservationItem.getTableNumber()))));

        List<Tables> tableItems = mapper.query(Tables.class, queryExpression);
        System.out.println("TableItems : "+tableItems.toString());

        if (tableItems.isEmpty()) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }

        System.out.println("Checking Overlapping");
        // Check for overlapping reservations
        // In production use better approach with index on 'Reservations' table for efficient querying
        ScanResult scanResult = client.scan(new ScanRequest().withTableName(ReservationsTable)); // Replace with the name of your 'Reservations' table
        System.out.println("Scanned successfully, checking for overlapping");
        for (Map<String, AttributeValue> item : scanResult.getItems()) {
            if (item.get("tableNumber").getN().equals(String.valueOf(reservationItem.getTableNumber())) &&
                    !(item.get("slotTimeEnd").getS().compareTo(reservationItem.getSlotTimeStart()) < 0 ||
                            item.get("slotTimeStart").getS().compareTo(reservationItem.getSlotTimeEnd()) > 0)&&
                    (item.get("date").getS().equals(reservationItem.getDate()))) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400);
            }
        }


        String uuid = UUID.randomUUID().toString();
        reservationItem.setId(uuid);
        System.out.println("Saving Reservations");
        mapper.save(reservationItem);

        System.out.println("Returning");
        // Construct response
        Map<String,String> responseStruct = new HashMap<>();
        responseStruct.put("reservationId", reservationItem.getId());
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(new Gson().toJson(responseStruct));

        return response;
    }


    public APIGatewayProxyResponseEvent getAllReservations() {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ReservationsTable);

        ScanResult result = ddb.scan(scanRequest);
        List<Reservations> reservations = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()){
            Reservations reservation = new Reservations(); // assuming you have a POJO class 'Table' with these fields and their setters & getters
            reservation.setId(item.get("id").getS());
            reservation.setTableNumber(Integer.parseInt(item.get("tableNumber").getN()));
            reservation.setClientName(item.get("clientName").getS());
            reservation.setPhoneNumber(item.get("phoneNumber").getS());
            reservation.setDate(item.get("date").getS());
            reservation.setSlotTimeStart(item.get("slotTimeStart").getS());
            reservation.setSlotTimeEnd(item.get("slotTimeEnd").getS());

            reservations.add(reservation);
        }

        Map<String, List<Reservations>> responseStruct = new HashMap<>();
        responseStruct.put("reservations", reservations);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(new Gson().toJson(responseStruct));

        return response;
    }

}




//String startDate = reservationPost.getSlotTimeStart();
//String endDate = reservationPost.getSlotTimeEnd();
//
//Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//eav.put(":v1", new AttributeValue().withN(Integer.toString(tableNumber)));
//        eav.put(":v2", new AttributeValue().withS(startDate));
//        eav.put(":v3", new AttributeValue().withS(endDate));
//
//DynamoDBQueryExpression<Reservations> queryExpression = new DynamoDBQueryExpression<Reservations>()
//        .withIndexName("yourGSIName") // Replace with the name of your GSI
//        .withKeyConditionExpression("tableNumber = :v1 and slotTimeStart between :v2 and :v3")
//        .withExpressionAttributeValues(eav)
//        .withConsistentRead(false); // GSI query needs to have ConsistentRead set to false
//
//List<Reservations> reservationItems = mapper.query(Reservations.class, queryExpression);
//
//if (!reservationItems.isEmpty()) {
//        return buildResponse(400, "Overlapping reservation exists for table number: " + reservationItem.getTableNumber());
//        }


//String startDate = reservationItem.getDate() + "T" + reservationItem.getSlotTimeStart();
//String endDate = reservationItem.getDate() + "T" + reservationItem.getSlotTimeEnd();