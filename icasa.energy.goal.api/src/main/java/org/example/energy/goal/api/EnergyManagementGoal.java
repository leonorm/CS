package org.example.energy.goal.api;

/**
 * Created by aygalinc on 09/11/16.
 */
public enum EnergyManagementGoal {

    /** The goal associated with soft illuminance. */
    SOFT(1),
    /** The goal associated with medium illuminance. */
    MEDIUM(2),
    /** The goal associated with full illuminance. */
    FULL(3);

    /** The number of lights to turn on. */
    private int energyManagementGoal;

    /**
     * Gets the number of lights to turn On.
     *
     * @return the number of lights to turn On.
     */
    public int getEnergyManagementGoal() {
        return this.energyManagementGoal;
    }

    private EnergyManagementGoal(int goal) {
        this.energyManagementGoal = goal;
    }

}
