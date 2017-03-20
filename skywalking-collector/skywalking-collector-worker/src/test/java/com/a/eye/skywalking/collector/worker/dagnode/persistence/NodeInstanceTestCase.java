package com.a.eye.skywalking.collector.worker.dagnode.persistence;

import com.a.eye.skywalking.collector.actor.AbstractWorker;
import com.a.eye.skywalking.collector.actor.LocalSyncWorkerRef;
import com.a.eye.skywalking.collector.worker.storage.EsClient;
import com.a.eye.skywalking.collector.worker.nodeinst.NodeInstIndex;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pengys5
 */
public class NodeInstanceTestCase {

    @Before
    public void initIndex() throws UnknownHostException {
        EsClient.boot();
        NodeInstIndex index = new NodeInstIndex();
        index.deleteIndex();
        index.createIndex();
    }

    @Test
    public void testLoadNodeInstance() throws Exception {
        loadNodeInstance(201703101201l, NodeInstIndex.Type_Minute);
        loadNodeInstance(201703101200l, NodeInstIndex.Type_Hour);
        loadNodeInstance(201703100000l, NodeInstIndex.Type_Day);
    }

    public void loadNodeInstance(long timeSlice, String type) throws Exception {
        LocalSyncWorkerRef workerRef = (LocalSyncWorkerRef) NodeInstanceSearchPersistence.Factory.INSTANCE.create(AbstractWorker.noOwner());

        insertData(timeSlice, type);
        EsClient.indexRefresh(NodeInstIndex.Index);

        NodeInstanceSearchPersistence.RequestEntity requestEntity = new NodeInstanceSearchPersistence.RequestEntity(type, timeSlice);
        JsonObject resJsonObj = new JsonObject();
        workerRef.ask(requestEntity, resJsonObj);
        JsonArray nodeArray = resJsonObj.get("result").getAsJsonArray();
        for (int i = 0; i < nodeArray.size(); i++) {
            JsonObject node = nodeArray.get(i).getAsJsonObject();
            System.out.println(node);
        }
    }

    private void insertData(long timeSlice, String type) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("code", "WebApplication");
        json.put("address", "10.218.9.86:8080");
        json.put("timeSlice", timeSlice);

        String _id = timeSlice + "-WebApplication-" + "10.218.9.86:8080";
        IndexResponse response = EsClient.getClient().prepareIndex(NodeInstIndex.Index, type, _id).setSource(json).get();
        RestStatus status = response.status();
        status.getStatus();

        json = new HashMap<String, Object>();
        json.put("code", "MotanServiceApplication");
        json.put("address", "10.20.3.15:3000");
        json.put("timeSlice", timeSlice);

        _id = timeSlice + "-MotanServiceApplication-" + "10.20.3.15:3000";
        response = EsClient.getClient().prepareIndex(NodeInstIndex.Index, type, _id).setSource(json).get();
        status = response.status();
        status.getStatus();
    }
}
