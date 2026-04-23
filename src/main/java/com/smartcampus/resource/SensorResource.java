/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 3 - Sensor Operations and Linking
 * Manages /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors — list sensors, optional ?type= filter (200 OK)
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<Sensor>();
        for (Sensor s : store.getSensors().values()) {
            if (type == null || s.getType().equalsIgnoreCase(type)) {
                result.add(s);
            }
        }
        return Response.ok(result).build();
    }

    // POST /api/v1/sensors — register a new sensor (201 | 400 | 409 | 422)
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(400, "Bad Request", "Sensor 'id' field is required."))
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(400, "Bad Request", "Sensor 'type' field is required."))
                    .build();
        }
        // 422: error is inside the request body — the roomId reference is invalid
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "roomId '" + sensor.getRoomId() + "' does not exist. "
                + "Register the room first before assigning sensors to it."
            );
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(409, "Conflict",
                            "Sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }
        store.getSensors().put(sensor.getId(), sensor);
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build())
                .entity(sensor)
                .build();
    }

    // GET /api/v1/sensors/{sensorId} — get sensor by ID (200 | 404)
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    /**
     * Part 4.1 - Sub-Resource Locator
     * Validates parent sensor exists, then delegates to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!store.getSensors().containsKey(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
        }
        return new SensorReadingResource(sensorId, store);
    }
}
