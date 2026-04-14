/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

/**
 * Thrown when DELETE is attempted on a room that still has sensors assigned.
 * Maps to HTTP 409 Conflict.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
