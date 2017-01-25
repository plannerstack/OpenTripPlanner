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

    /** Choose short or long form of results. */
    @QueryParam("detail") private boolean detail = false;

    Router router;

    public UpdaterStatusResource (@Context OTPServer otpServer, @PathParam("routerId") String routerId) {
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

    /** Return a list of all agencies in the graph. */
    @GET
    @Path("/agency/{feedId}")
    public Response getAgencies (@PathParam("feedId") String feedId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There is no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAgency(feedId)).build();
    }

    //TODO does not look good
    /** Return status for a specific updater. */
    @GET
    @Path("/{updaterId}")
    public Response getTypeOfUpdater (@PathParam("updaterId") int updaterId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getUpdater(updaterId)).build();
    }


    //TODO
    @GET
    @Path("/stream")
    public Response getStreamAddresses() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getStreamAddresses()).build();
    }


    @GET
    @Path("/types")
    public Response getTypes() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAllTypes()).build();
    }

    //TODO all updates
    @GET
    @Path("/updates")
    public Response getUpdates () {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getUpdates()).build();
    }

    @GET
    @Path("/updates/types")
    public Response getUpdatesTypes() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getUpdatesTypes()).build();
    }

    //TODO
    @GET
    @Path("/updates/applied")
    public Response getApplied() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getApplied()).build();
    }

    @GET
    @Path("/updates/errors")
    public Response getErrors() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getErrors()).build();
    }

    @GET
    @Path("/updates/received")
    public Response getReceived() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getReceived()).build();
    }

    @GET
    @Path("/updates/received/last")
    public Response getLastReceived() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getLastReceived()).build();
    }

    @GET
    @Path("/updates/updated/last")
    public Response getLastUpdated() {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getLastApplied()).build();
    }

    //TODO
    @GET
    @Path("/feed/{feedId}/trip/{tripId}")
    public Response getUpdatesPerFeed (@PathParam("feedId") int feedId, @PathParam ("tripId") int tripId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getTypeAppliedPerFeedPerTrip(feedId, tripId)).build();
    }

    //TODO
    @GET
    @Path("applied/feed/{feedId}")
    public Response getAppliedPerFeed(@PathParam("feedId") int feedId) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAppliedPerFeed(feedId)).build();
    }


    @GET
    @Path("applied/{lastMinutes}")
    public Response getAppliedLastMinutes(@PathParam("lastMinutes") int minutes) {
        GraphUpdaterManager updaterManager = router.graph.updaterManager;
        if (updaterManager == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("There are no updaters running.").build();
        }
        return Response.status(Response.Status.OK).entity(updaterManager.getAppliedLastMinutes(minutes)).build();
    }





}
