package com.kazurayam.materialstore.filesystem;

import java.util.Map;

public interface GraphvizReady {

    /**
     * assumes sequenceNumber is 0
     * assumes standalone is true
     */
    String toDot();

    /**
     * generates `subgraph nameX {...}` where X is interpolated with sequenceNumber
     * @param sequenceNumber should be 0, 1, 2, ... that makes the subgraph name unique in the entire digraph
     */
    String toDot(Map<String, String> options);

    /**
     * generates `digraph G {...}` lines which encloses the graph of this object
     * @param standalone
     * @return
     */
    String toDot(boolean standalone);

    String toDot(Map<String, String> options, boolean standalone);

}
