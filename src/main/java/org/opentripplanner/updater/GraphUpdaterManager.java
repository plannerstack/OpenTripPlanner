/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.updater;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.stoptime.TimetableSnapshotSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is attached to the graph:
 *
 * <pre>
 * GraphUpdaterManager updaterManager = graph.getUpdaterManager();
 * </pre>
 *
 * Each updater will run in its own thread. When changes to the graph have to be made by these
 * updaters, this should be done via the execute method of this manager to prevent race conditions
 * between graph write operations.
 *
 */
public class GraphUpdaterManager {

    private static Logger LOG = LoggerFactory.getLogger(GraphUpdaterManager.class);

    /**
     * Text used for naming threads when the graph lacks a routerId.
     */
    private static String DEFAULT_ROUTER_ID = "(default)";

    /**
     * Thread factory used to create new threads.
     */

    private ThreadFactory threadFactory;

    /**
     * OTP's multi-version concurrency control model for graph updating allows simultaneous reads,
     * but never simultaneous writes. We ensure this policy is respected by having a single writer
     * thread, which sequentially executes all graph updater tasks. Each task is a runnable that is
     * scheduled with the ExecutorService to run at regular intervals.
     */
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Pool with updaters
     */
    private ExecutorService updaterPool = Executors.newCachedThreadPool();

    /**
     * List with updaters to be able to free resources TODO: is this list necessary?
     */
    List<GraphUpdater> updaterList = new ArrayList<GraphUpdater>();

    /**
     * Parent graph of this manager
     */
    Graph graph;
    TimetableSnapshotSource snapshot;
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor
     *
     * @param graph is parent graph of manager
     */
    public GraphUpdaterManager(Graph graph) {
        this.graph = graph;

        String routerId = graph.routerId;
        if(routerId == null || routerId.isEmpty())
            routerId = DEFAULT_ROUTER_ID;

        threadFactory = new ThreadFactoryBuilder().setNameFormat("GraphUpdater-" + routerId + "-%d").build();
        scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        updaterPool = Executors.newCachedThreadPool(threadFactory);

    }

    public void stop() {
        // TODO: find a better way to stop these threads

        // Shutdown updaters
        updaterPool.shutdownNow();
        try {
            boolean ok = updaterPool.awaitTermination(30, TimeUnit.SECONDS);
            if (!ok) {
                LOG.warn("Timeout waiting for updaters to finish.");
            }
        } catch (InterruptedException e) {
            // This should not happen
            LOG.warn("Interrupted while waiting for updaters to finish.");
        }

        // Clean up updaters
        for (GraphUpdater updater : updaterList) {
            updater.teardown();
        }
        updaterList.clear();

        // Shutdown scheduler
        scheduler.shutdownNow();
        try {
            boolean ok = scheduler.awaitTermination(30, TimeUnit.SECONDS);
            if (!ok) {
                LOG.warn("Timeout waiting for scheduled task to finish.");
            }
        } catch (InterruptedException e) {
            // This should not happen
            LOG.warn("Interrupted while waiting for scheduled task to finish.");
        }
    }

    /**
     * Adds an updater to the manager and runs it immediately in its own thread.
     *
     * @param updater is the updater to add and run
     */
    public void addUpdater(final GraphUpdater updater) {
        updaterList.add(updater);
        updaterPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    updater.setup();
                    try {
                        updater.run();
                    } catch (Exception e) {
                        LOG.error("Error while running updater {}:", updater.getClass().getName(), e);
                    }
                } catch (Exception e) {
                    LOG.error("Error while setting up updater {}:", updater.getClass().getName(), e);
                }
            }
        });
    }

    /**
     * This is the method to use to modify the graph from the updaters. The runnables will be
     * scheduled after each other, guaranteeing that only one of these runnables will be active at
     * any time.
     *
     * @param runnable is a graph writer runnable
     */
    public void execute(GraphWriterRunnable runnable) {
        executeReturningFuture(runnable);
    }

    /**
     * This is another method to use to modify the graph from the updaters. It behaves like execute,
     * but blocks until the runnable has been executed. This might be particularly useful in the 
     * setup method of an updater.
     *
     * @param runnable is a graph writer runnable
     * @throws ExecutionException
     * @throws InterruptedException
     * //@see GraphUpdaterManager.execute
     */
    public void executeBlocking(GraphWriterRunnable runnable) throws InterruptedException,
            ExecutionException {
        Future<?> future = executeReturningFuture(runnable);
        // Ask for result of future. Will block and return null when runnable is successfully
        // finished, throws otherwise
        future.get();
    }

    private Future<?> executeReturningFuture(final GraphWriterRunnable runnable) {
        // TODO: check for high water mark?  kada ostane mnogo praznih polja posle brisanja 14841
        Future<?> future = scheduler.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run(graph);
                } catch (Exception e) {
                    LOG.error("Error while running graph writer {}:", runnable.getClass().getName(),
                            e);
                }
            }
        });
        return future;
    }

    public int size() {
        return updaterList.size();

    }

    /**
     * Just an example of fetching status information from the graph updater manager to expose it in a web service.
     * More useful stuff should be added later.
     * @return All stream addresses
     */
    private Map<Integer, String> getAllStreamAddresses() {
        Map<Integer, String> ret = new HashMap<>();
        int i = 0;

        for (GraphUpdater updater : updaterList) {
            ret.put(i++, updater.toString());
        }
        return ret;
    }

    /**
     * @return correct and all stream addresses
     */
    public String getStreamAddresses() {
        try {
            Map<String, Object> combined = new HashMap<>();
            combined.put("Correct stream addresses", graph.getCorrectStreamAddresses());
            combined.put("All stream addresses", getAllStreamAddresses());
            return mapper.writeValueAsString(combined);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    /**
     *
     * @return most of the important data
     */
    //TODO 14841 Extract ObjectMapper related code to one method
    public String getDescription() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }


    /**
     * Just an example of fetching status information from the graph updater manager to expose it in a web service.
     * More useful stuff should be added later.
     */
    public String getUpdater(int id) {
        String ret = "Updater does not exist.";
        if (id < updaterList.size())
            ret = updaterList.get(id).toString();
        try {
            return mapper.writeValueAsString(ret);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    /**
     * @param id of GraphUpdater whose type is required
     * @return type of GraphUpdater
     */
    public String getType(int id) {
        GraphUpdater updater = null;
        if (id < updaterList.size())
            updater = updaterList.get(id);

        try {
            return mapper.writeValueAsString(updater==null?"No updater.":updater.getClass().toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    /**
     * @return all types of updaters
     */
    public String getAllTypes() {
        HashMap<Integer, String> retVal = new HashMap<>();
        int i = 0;
        for (GraphUpdater up : updaterList)
            retVal.put(i++, up.getClass().getName());
        try {
            return mapper.writeValueAsString(retVal);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }


    /**
     * @return all received updates
     */
    public String getReceived() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getReceived());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    /**
     * @return all applied updates
     */

    public String getApplied() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getApplied());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    /**
     * @return all non-applied updates
     */

    public String getErrors() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getErrors());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    public String getUpdatesTypes() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getTypes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }

    }

    //TODO 14841 String representation of time
    public String getLastApplied() {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getLastUpdate());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }

    }

    //TODO 14841
    public String getLastReceived() {

        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getLastReceived());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }

    }


    //TODO 14841
    public List<String> getAppliedPerFeed(int feedId) {
        return null;
    }

    //TODO 14841 what is tripId
    public List<String> getTypeAppliedPerFeedPerTrip(int feedId, int tripId) {
        return null;
    }

    //TODO 14841 what is stream
    public List<String> getTotalPerStream() {
        return null;
    }

    //TODO 14841 think of something
    public List<String> getSomethingNew() {


        return null;
    }

    //TODO 14841
    public String getUpdates() {
        return null;
    }


    //TODO 14841
    public String getAgency(String feedId) {
        try {
            return mapper.writeValueAsString(graph.getAgencies(feedId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }

    public String getAppliedLastMinutes(int minutes) {
        TimetableSnapshotSource snap = graph.timetableSnapshotSource;
        try {
            return mapper.writeValueAsString(snap.getAppliedLastMinutes(minutes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Something went wrong with Json.";
        }
    }
}
