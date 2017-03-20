package com.a.eye.skywalking.collector.worker.nodeinst.persistence;

import com.a.eye.skywalking.collector.actor.*;
import com.a.eye.skywalking.collector.actor.selector.HashCodeSelector;
import com.a.eye.skywalking.collector.actor.selector.WorkerSelector;
import com.a.eye.skywalking.collector.worker.WorkerConfig;
import com.a.eye.skywalking.collector.worker.storage.RecordData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pengys5
 */
public class NodeInstHourAgg extends AbstractClusterWorker {

    private Logger logger = LogManager.getFormatterLogger(NodeInstHourAgg.class);

    public NodeInstHourAgg(com.a.eye.skywalking.collector.actor.Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    public void preStart() throws ProviderNotFoundException {
        getClusterContext().findProvider(NodeInstHourSave.Role.INSTANCE).create(this);
    }

    @Override
    protected void onWork(Object message) throws Exception {
        if (message instanceof RecordData) {
            getSelfContext().lookup(NodeInstHourSave.Role.INSTANCE).tell(message);
        } else {
            logger.error("message unhandled");
        }
    }

    public static class Factory extends AbstractClusterWorkerProvider<NodeInstHourAgg> {
        public static Factory INSTANCE = new Factory();

        @Override
        public Role role() {
            return Role.INSTANCE;
        }

        @Override
        public NodeInstHourAgg workerInstance(ClusterWorkerContext clusterContext) {
            return new NodeInstHourAgg(role(), clusterContext, new LocalWorkerContext());
        }

        @Override
        public int workerNum() {
            return WorkerConfig.Worker.NodeInstanceReceiver.Num;
        }
    }

    public enum Role implements com.a.eye.skywalking.collector.actor.Role {
        INSTANCE;

        @Override
        public String roleName() {
            return NodeInstHourAgg.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new HashCodeSelector();
        }
    }
}
