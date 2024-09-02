# SmartContract

This is a lightweight smart contract template system that leverages Java and Groovy scripts to manage business logic 
computations during various stages of contract settlements. It stores contract settlement snapshots in MongoDB.

The modular architecture of this system can also be adapted to handle other scenarios involving complex computational 
logic.

### Instructions for local testing with Postman:
**Test Data**

Input messages stored in /data folder.

**API testing example**
1. Create a POST request with url: http://localhost:8989/smartContract/message/route.
2. Set body in raw json format, copy test data and paste here.

**Update and save groovy handler scripts**
1. Create a GET request with url: http://localhost:8989/smartContract/groovyScript/save?handlerNames=OneForAll.
2. Set Param "handlerNames" as List<String>
3. OneForAll: update all groovy handler scripts, AvgPrice/FixedPricing: update the corresponding script.
    
