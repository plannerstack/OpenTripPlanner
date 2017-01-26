package org.opentripplanner.api.resource;

import com.google.transit.realtime.GtfsRealtime;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Report the status of the graph updaters via a web service.
 */
@Path("/routers/{routerId}/updaters")
@Produces(MediaType.APPLICATION_JSON)
public class UpdaterStatusResource {


    private static final Logger LOG = LoggerFactory.getLogger(UpdaterStatusResource.class);

    /**
     * Choose short or long form of results.
     */
    @QueryParam("detail")
    private boolean detail = false;

    Router router;

    public UpdaterStatusResource(@Context OTPServer otpServer, @PathParam("routerId") String routerId) {
        router = otpServer.getRouter(routerId);
    }

    /**
     *
     * @return most of the important data calculated by TimetableSnapshotSource
     */
    @GET
    public Response getDescription() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getDescription()).build();
    }

    /**
     *
     * @return a list of all agencies in the graph for the given feedId.
     */
    @GET
    @Path("/agency/{feedId}")
    public Response getAgencies(@PathParam("feedId") String feedId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There is no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAgency(feedId)).build();
    }

    /**
     *
     * @return status for a specific updater.
     */
    @GET
    @Path("/{updaterId}")
    public Response getTypeOfUpdater(@PathParam("updaterId") int updaterId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getUpdater(updaterId)).build();
    }

    /**
     *
     * @return correct and all stream addresses
     */
    @GET
    @Path("/stream")
    public Response getStreamAddresses() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getStreamAddresses()).build();
    }

    /**
     *
     * @return types of updater
     */
    @GET
    @Path("/types")
    public Response getTypes() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAllTypes()).build();
    }

    /**
     *
     * @param updaterId
     * @return type of updater for updaterId
     */
    @GET
    @Path("/types/{id}")
    public Response getTypePerId(@PathParam("id") int updaterId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getType(updaterId)).build();
    }

    /**
     *
     * @return all updates grouped by tripid,
     * exposing the number of time tripid showed in updates
     */
    @GET
    @Path("/updates")
    public Response getUpdates() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getReceived()).build();
    }

    /**
     *
     * @return the number of updates per type
     */
    @GET
    @Path("/updates/types")
    public Response getUpdatesTypes() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getUpdatesTypes()).build();
    }

    /**
     *
     * @return the number of updates applied per tripId
     */
    @GET
    @Path("/updates/applied")
    public Response getApplied() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getApplied()).build();
    }

    /**
     *
     * @return the errors for updates grouped by its String representation
     * exposing the number of occurrences for each error
     */
    @GET
    @Path("/updates/errors")
    public Response getErrors() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getErrors()).build();
    }

    /**
     *
     * @return the errors for last block of updates
     */
    @GET
    @Path("/updates/errors/last")
    public Response getLastErrors() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getLastErrors()).build();
    }

    /**
     *
     * @return time and tripId for the last received and applied update
     */
    @GET
    @Path("/updates/last")
    public Response getLastAppliedReceived() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getLastAppliedReceived()).build();
    }

    /**
     *
     * @return the ratio between received and applied update
     */
    @GET
    @Path("/updates/ratio")
    public Response getReceivedApplied() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getReceivedApplied()).build();
    }

    /**
     *
     * @param feedId
     * @param tripId
     * @return the number of updates for feedId and tripId grouped by type
     */
    @GET
    @Path("/updates/applied/feed/{feedId}/trip/{tripId}")
    public Response getUpdatesPerFeedPerTrip(@PathParam("feedId") String feedId, @PathParam("tripId") String tripId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getTypeAppliedPerFeedPerTrip(feedId, tripId)).build();
    }

    /**
     *
     * @param feedId
     * @return the number of updates applied for feedId
     */
    @GET
    @Path("/updates/applied/feed/{feedId}")
    public Response getAppliedPerFeed(@PathParam("feedId") String feedId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAppliedPerFeed(feedId)).build();
    }

    /**
     *
     * @param minutes
     * @return information about updates that occurred
     * in the last number of minutes
     */
    @GET
    @Path("updates/applied/{lastMinutes}")
    public Response getAppliedLastMinutes(@PathParam("lastMinutes") int minutes) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAppliedLastMinutes(minutes)).build();
    }
}
