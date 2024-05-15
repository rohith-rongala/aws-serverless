package com.task10.pojo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

@Data
@DynamoDBTable(tableName = "cmtr-aa756657-Tables-test")
public class Tables {
    @DynamoDBHashKey
    private int id;
    private int number;
    private int places;
    private boolean isVip;
    private int minOrder;
}
