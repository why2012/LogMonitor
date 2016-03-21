package com.logmonitor.processor;

import com.logmonitor.processor.bolt.IndexBolt;
import com.logmonitor.processor.bolt.ParserBolt;
import com.logmonitor.processor.spout.SourceSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wanghaiyang on 16/3/16.
 */
public class TopologyMain {
    private static final String SOURCE_SPOUT = "SourceSpout";
    private static final String PARSER_BOLT = "ParseBolt";
    private static final String INDEX_BOLT = "IndexBolt";
    private boolean local = false;
    private String topologyName = "DEFAULT_LOG_PROCESS_TOPOLOGY";

    public TopologyBuilder getTopology() {
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout(SOURCE_SPOUT, new SourceSpout(), 5);
        topologyBuilder.setBolt(PARSER_BOLT, new ParserBolt(), 5).shuffleGrouping(SOURCE_SPOUT);
        topologyBuilder.setBolt(INDEX_BOLT, new IndexBolt(), 5).shuffleGrouping(PARSER_BOLT);

        return topologyBuilder;
    }

    public Map getConf() {
        Map conf = new HashMap();
        conf.put(Config.TOPOLOGY_WORKERS, 4);
        return conf;
    }

    public LocalCluster submitLocalTopology(StormTopology stormTopology, Config config) {
        LocalCluster localCluster = new LocalCluster();
        localCluster.submitTopology(topologyName, config, stormTopology);

        return localCluster;
    }

    public StormSubmitter submitDevTopology(StormTopology stormTopology, Config config)
            throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        StormSubmitter stormSubmitter = new StormSubmitter();
        stormSubmitter.submitTopology(topologyName, config, stormTopology);

        return stormSubmitter;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }
}
