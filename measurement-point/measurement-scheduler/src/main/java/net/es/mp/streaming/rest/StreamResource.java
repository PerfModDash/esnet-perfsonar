package net.es.mp.streaming.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import net.es.mp.authn.AuthnSubject;
import net.es.mp.authz.AuthorizationException;
import net.es.mp.measurement.types.Measurement;
import net.es.mp.scheduler.NetLogger;
import net.es.mp.streaming.MPStreamingService;
import net.es.mp.streaming.types.Stream;
import net.es.mp.util.RESTAuthnUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Path("/mp/streams/{streamId}")
public class StreamResource {
    Logger log = Logger.getLogger(StreamResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    MPStreamingService globals = MPStreamingService.getInstance();
    final private String GET_EVENT = "mp.streaming.rest.StreamResource.get";
    final private String DELETE_EVENT = "mp.streaming.rest.StreamResource.delete";
    final private String PUT_EVENT = "mp.streaming.rest.StreamResource.put";
    @Context UriInfo uriInfo;
    
    @Produces("application/json")
    @GET
    public Response get(@PathParam("streamId") String streamId, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(GET_EVENT));
        
        //authenticate
        AuthnSubject authnSubject = RESTAuthnUtil.extractAuthnSubject(httpHeaders, 
                globals.getContainer().getAuthnSubjectFactory());
        
        //call stream manager
        Stream stream = null;
        try{
            stream = globals.getManager().getStream(streamId, authnSubject);
        }catch(AuthorizationException e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }catch(Exception e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
        
        //check for not found
        if(stream == null){
            return Response.status(Status.NOT_FOUND).entity("Stream resource not found").build();
        }
        
        //output JSON
        this.netLogger.info(netLog.end(GET_EVENT));
        return Response.ok().entity(stream.toJSONString()).build();
    }
    
    @Produces("application/json")
    @DELETE
    public Response delete(@PathParam("streamId") String streamId, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(DELETE_EVENT));
        
        //authenticate
        AuthnSubject authnSubject = RESTAuthnUtil.extractAuthnSubject(httpHeaders, 
                globals.getContainer().getAuthnSubjectFactory());
        
        //call stream manager
        boolean validResource = false;
        try{
            validResource = globals.getManager().deleteStream(streamId, authnSubject);
        }catch(AuthorizationException e){
            this.netLogger.error(netLog.error(DELETE_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }catch(Exception e){
            this.netLogger.error(netLog.error(DELETE_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
        
        //check for not found
        if(!validResource){
            return Response.status(Status.NOT_FOUND).entity("Stream resource not found").build();
        }
        
        //build response
        JSONObject response = new JSONObject();
        response.put("uri", uriInfo.getRequestUri().toASCIIString());
        
        //output JSON
        this.netLogger.info(netLog.end(DELETE_EVENT));
        return Response.ok().entity(response.toString()).build();
    }
    
    @Produces("application/json")
    @Consumes("application/json")
    @PUT
    public Response put(@PathParam("streamId") String streamId, String body, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(PUT_EVENT));
        
        //get subject
        AuthnSubject authnSubject = RESTAuthnUtil.extractAuthnSubject(httpHeaders, 
                globals.getContainer().getAuthnSubjectFactory());
        
        //parse input
        JSONObject jsonRequest = JSONObject.fromObject(body);
        List<Measurement> measurements = new ArrayList<Measurement>();
        if(!jsonRequest.containsKey(Stream.MEASUREMENTS) || jsonRequest.get(Stream.MEASUREMENTS) == null || 
                !(jsonRequest.get(Stream.MEASUREMENTS) instanceof JSONArray)){
            String errMsg = "Request does not contain valid '" + Stream.MEASUREMENTS + "' field";
            this.netLogger.info(netLog.error(PUT_EVENT, errMsg));
            this.log.error(errMsg);
            return Response.status(Status.BAD_REQUEST).entity(errMsg).build();
        }
        for(Object measObj : (JSONArray)jsonRequest.get(Stream.MEASUREMENTS)){
            try{
                measurements.add(new Measurement((DBObject)JSON.parse(((JSONObject)measObj).toString())));
            }catch(Exception e){
                String errMsg = "Invalid measurement object in request";
                this.netLogger.info(netLog.error(PUT_EVENT, errMsg));
                this.log.error(errMsg);
                return Response.status(Status.BAD_REQUEST).entity(errMsg).build();
            }
        }
        
        //call steam manager
        boolean updateSucceeded = false;
        try{
            updateSucceeded = MPStreamingService.getInstance().getManager().addMeasurements(streamId, measurements, authnSubject);
        }catch(AuthorizationException e){
            this.netLogger.error(netLog.error(PUT_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }catch(Exception e){
            this.netLogger.error(netLog.error(PUT_EVENT, e.getMessage()));
            this.log.error(e.getMessage());
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
        if(!updateSucceeded){
            return Response.status(Status.NOT_FOUND).entity("Stream resource not found").build();
        }
        
        //build response
        JSONObject response = new JSONObject();
        response.put("uri", uriInfo.getRequestUri().toASCIIString());
        
        //output JSON
        this.netLogger.info(netLog.end(PUT_EVENT));
        return Response.ok().entity(response.toString()).build();

    }
}
