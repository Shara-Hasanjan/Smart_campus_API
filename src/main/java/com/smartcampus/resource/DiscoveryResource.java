/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 - Discovery Endpoint
 * GET /api/v1 returns API metadata with HATEOAS-style resource links.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("api", "Smart Campus Sensor and Room Management API");
        response.put("version", "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("timestamp", Instant.now().toString());

        Map<String, String> contact = new LinkedHashMap<String, String>();
        contact.put("name", "Campus Facilities Team");
        contact.put("email", "admin@smartcampus.ac.uk");
        contact.put("department", "Estates and Facilities Management");
        response.put("contact", contact);

        Map<String, Object> resources = new LinkedHashMap<String, Object>();
        resources.put("rooms",    buildLink("/api/v1/rooms",                       "GET, POST",   "List all rooms or create a new room"));
        resources.put("room",     buildLink("/api/v1/rooms/{roomId}",              "GET, DELETE", "Retrieve or decommission a specific room"));
        resources.put("sensors",  buildLink("/api/v1/sensors",                     "GET, POST",   "List sensors (supports ?type= filter) or register a new sensor"));
        resources.put("sensor",   buildLink("/api/v1/sensors/{sensorId}",          "GET",         "Retrieve a specific sensor by ID"));
        resources.put("readings", buildLink("/api/v1/sensors/{sensorId}/readings", "GET, POST",   "Get or append historical readings for a sensor"));
        response.put("resources", resources);

        return Response.ok(response).build();
    }

    private Map<String, Object> buildLink(String href, String methods, String description) {
        Map<String, Object> link = new LinkedHashMap<String, Object>();
        link.put("href", href);
        link.put("methods", methods);
        link.put("description", description);
        return link;
    }
}