package me.flame.storysmp;

import org.bukkit.Location;

public class TrackedLocation {
    private int traveled;
    private Location location;

    public TrackedLocation(int traveled, Location location) {
        this.traveled = traveled;
        this.location = location;
    }

    public int traveled() {
        return traveled;
    }

    public Location location() {
        return location;
    }

    public void setTraveled(int traveled) {
        this.traveled = traveled;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}