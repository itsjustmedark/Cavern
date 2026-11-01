package net.cavern.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class FacingUtils {

    private static String previousDirection = "Null";

    public static Vec3 getDirectionVec(float yaw, float pitch) {
        // Convert yaw and pitch to radians
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        // Calculate direction vector
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vec3(x, y, z);
    }

    public static String performActionBasedOnLooking(Entity entity) {
        // Get the yaw and pitch from the entity
        float yaw = entity.rotationYaw;
        float pitch = entity.rotationPitch;

        // Calculate the direction vector
        Vec3 direction = getDirectionVec(yaw, 0);

        // Determine the horizontal direction based on the yaw
        String horizontalDirection;
        if (direction.xCoord > 0.5) {
            previousDirection = horizontalDirection = "East";
        } else if (direction.xCoord < -0.5) {
            previousDirection = horizontalDirection = "West";
        } else if (direction.zCoord > 0.5) {
            previousDirection = horizontalDirection = "South";
        } else if (direction.zCoord < -0.5) {
            previousDirection = horizontalDirection = "North";
        } else {
            horizontalDirection = previousDirection; // Handle ambiguous directions
        }

        // Return the horizontal direction regardless of pitch
        return horizontalDirection;
    }

}
