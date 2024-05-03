package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;

import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(batchSize = 3,targetTable = "Configuration")
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private final DynamoDB dynamoDB;
	private final Table auditTable;

	public AuditProducer() {
		this.dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
		this.auditTable = dynamoDB.getTable("cmtr-aa756657-Audit-test");
	}

	@Override
	public Void handleRequest(DynamodbEvent event, Context context) {
		for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
			com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord streamRecord = record.getDynamodb();
			if ("INSERT".equals(record.getEventName())) {
				handleInsertEvent(streamRecord);
			} else if ("MODIFY".equals(record.getEventName())) {
				handleModifyEvent(streamRecord);
			}
		}
		return null;
	}


	private void handleInsertEvent(com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord streamRecord) {
		String itemId = streamRecord.getKeys().get("key").getS();
		Map<String, Object> newValue = getNewValue(streamRecord);
		String modificationTime = LocalDateTime.now().toString();

		createAuditEntry(itemId, modificationTime, newValue);
	}

	private void handleModifyEvent(com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord streamRecord) {
		String itemId = streamRecord.getKeys().get("key").getS();
		Map<String, Object> oldValue = getOldValue(streamRecord);
		Map<String, Object> newValue = getNewValue(streamRecord);
		String modificationTime = LocalDateTime.now().toString();

		createAuditEntry(itemId, modificationTime, oldValue, newValue);
	}

	private Map<String, Object> getOldValue(com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord streamRecord) {
		Map<String, Object> oldValues = new HashMap<>();
//		com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord abc = streamRecord.getOldImage().entrySet();
		for (Map.Entry<String,com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> entry : streamRecord.getOldImage().entrySet()) {
			oldValues.put(entry.getKey(), entry.getValue().getS());
		}
		return oldValues;
	}

	private Map<String, Object> getNewValue(com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord streamRecord) {
		Map<String, Object> newValues = new HashMap<>();
		for (Map.Entry<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> entry : streamRecord.getNewImage().entrySet()) {
			newValues.put(entry.getKey(), entry.getValue().getN());
		}
		return newValues;
	}

	private void createAuditEntry(String itemId, String modificationTime, Map<String, Object> newValue) {
		Item auditEntry = new Item()
				.withString("id", UUID.randomUUID().toString())
				.withString("itemKey", itemId)
				.withString("modificationTime", modificationTime)
				.withMap("newValue", newValue);

		PutItemSpec putItemSpec = new PutItemSpec().withItem(auditEntry);
		auditTable.putItem(putItemSpec);
	}

	private void createAuditEntry(String itemId, String modificationTime, Map<String, Object> oldValue, Map<String, Object> newValue) {
		Map<String, Object> auditEntryMap = new HashMap<>();
		auditEntryMap.put("id", UUID.randomUUID().toString());
		auditEntryMap.put("itemKey", itemId);
		auditEntryMap.put("modificationTime", modificationTime);
		auditEntryMap.put("oldValue", oldValue);
		auditEntryMap.put("newValue", newValue);

		Item auditEntry = Item.fromMap(auditEntryMap);
		PutItemSpec putItemSpec = new PutItemSpec().withItem(auditEntry);
		auditTable.putItem(putItemSpec);
	}
}
