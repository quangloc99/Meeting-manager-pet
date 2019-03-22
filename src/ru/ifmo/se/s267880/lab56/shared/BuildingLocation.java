package ru.ifmo.se.s267880.lab56.shared;

import java.io.Serializable;

public class BuildingLocation implements Serializable {
    private int buildingNumber;  // replacement for X coordinates
    private int floor;           // replacement for Y coordinates

    public BuildingLocation(int buildingNumber, int floor) {
        this.buildingNumber = buildingNumber;
        this.floor = floor;
    }

    public String toString() {
        return String.format("%d-th floor of %d-th building", floor, buildingNumber);
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public int getFloor() {
        return floor;
    }
}
