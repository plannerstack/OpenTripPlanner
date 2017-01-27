package org.opentripplanner.api.resource;

import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
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

    /**
     *
     * @param otpServer
     * @param routerId id of the router
     */
    public UpdaterStatusResource(@Context OTPServer otpServer, @PathParam("routerId") String routerId) {
        router = otpServer.getRouter(routerId);
    }

    /**
     *
     * Returns view of the important data on real time status updates.
     *
     * @return general information about updates(errors of the updates, types
     * of updates, received and applied updates)
     *
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
     * Returns all agencies for specific feed.
     * @param feedId id of the feed
     * @return a list of all agencies in the graph for the specific feed,
     * if such exist otherwise return null
     */
    @GET
    @Path("/agency/{feedId}")
    public Response getAgencies(@PathParam("feedId") String feedId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAgency(feedId)).build();
    }

    /**
     *
     * Returns information on specific updater.
     * @param updaterId id of the updater
     * @return Description for the updater with updater id.
     * If such does not exist "Updater does not exist." is reported.
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
     * Returns information on stream addresses.
     * @return set of all correct stream addresses and list of information on all
     * stream addresses.
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
     * Returns types of updaters.
     * @return a list of all types of updaters
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
     * Returns short information on the updater.
     * @param updaterId id of the updater
     * @return type of updater for updater id,
     * if such does not exist "No updater." is reported
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
     * Returns a list of received updates.
     * @return all updates grouped by trip id,
     * exposing the number of times each trip was updated
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
     * Returns the number of updates per type.
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
     * Returns the number of applied updates.
     * @return the number of applied updates grouped by trip id
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
     * Returns errors that occurred for non-applied updates.
     * @return errors for updates grouped by its string representation
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
     * Returns errors that occurred for last block of non-applied updates.
     * @return errors for the last block of updates
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
     *  Returns information on the last updated trip.
     * @return time when update occurred, id of the trip being updated for
     * each last received and last applied update
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
     * Returns the ratio between received and applied updates.
     * @return the number of received and applied updates
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
     * Returns the number of applied updates for feed and trip.
     * @param feedId id of the feed
     * @param tripId id of the trip
     * @return the number of updates for specific feed and trip, grouped by type
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
     * Returns the number of applied updates for provided feed.
     * @param feedId id of the feed
     * @return the number of applied updates for specific feed
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
     * Returns the information on applied updates not older than provided parameter.
     * @param minutes the number of minutes (all updates older than 60 minutes are discarded)
     * @return information about applied updates that occurred in the last number of minutes
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
