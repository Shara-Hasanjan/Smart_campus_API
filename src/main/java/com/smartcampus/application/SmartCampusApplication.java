/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.application;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application entry point for Tomcat deployment.
 * Base URI is set to /api/v1 via web.xml mapping.
 * ResourceConfig handles automatic package scanning for all
 * resources, exception mappers, and filters.
 */
@ApplicationPath("/")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        packages(
            "com.smartcampus.resource",
            "com.smartcampus.exception",
            "com.smartcampus.filter"
        );
    }
}