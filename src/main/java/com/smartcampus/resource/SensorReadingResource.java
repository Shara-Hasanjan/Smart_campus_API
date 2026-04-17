/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Part 4 - Sub-Resource for sensor readings.
 * Handles GET and POST for /api/v1/sensors/{sensorId}/readings
 * Instantiated by SensorResource via the sub-resource locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store;

    public SensorReadingResource(String sensorId, DataStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    // GET /api/v1/sensors/{sensorId}/readings — fetch reading history (200 OK)
    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadingsForSensor(sensorId);
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings — add new reading (201 | 400 | 403)
    // Side effect: updates parent Sensor.currentValue (Part 4.2)
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);

        // Part 5.3 — block readings for MAINTENANCE sensors
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE "
                + "and cannot accept new readings."
            );
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Request body is required with a numeric 'value' field."))
                    .build();
        }

        SensorReading newReading = new SensorReading(reading.getValue());
        store.addReading(sensorId, newReading);

        // Part 4.2 side effect — keep parent sensor currentValue in sync
        sensor.setCurrentValue(newReading.getValue());

        return Response
                .created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + newReading.getId()))
                .entity(newReading)
                .build();
    }
}