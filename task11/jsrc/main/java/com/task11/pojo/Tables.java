package com.task11.pojo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.task11.util.Constants;
import lombok.Data;

@Data
@DynamoDBTable(tableName = Constants.TABLES_NAME)
public class Tables {
    @DynamoDBHashKey
    private int id;
    private int number;
    private int places;
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    private boolean isVip;
    private int minOrder;

    @Override
    public String toString() {
        return "Tables{" +
                "id=" + id +
                ", number=" + number +
                ", places=" + places +
                ", isVip=" + isVip +
                ", minOrder=" + minOrder +
                '}';
    }
}
