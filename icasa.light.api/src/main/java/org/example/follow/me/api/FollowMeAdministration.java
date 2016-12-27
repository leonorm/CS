package org.example.follow.me.api;

/**
 * Created by aygalinc on 09/11/16.
 */
public interface FollowMeAdministration {

    /**
     * Sets the illuminance preference. The manager will try to adjust the
     * illuminance in accordance with this goal.
     *
     * @param illuminanceGoal
     *            the new illuminance preference
     */
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal);

    /**
     * Get the current illuminance preference.
     *
     * @return the new illuminance preference
     */
    public IlluminanceGoal getIlluminancePreference();
    
    /**
     * Configure the energy saving goal.
     * @param energyGoal : the targeted energy goal.
     */
    public void setEnergySavingGoal(EnergyGoal energyGoal);
 
    /**
     * Gets the current energy goal.
     * 
     * @return the current energy goal.
     */
    public EnergyGoal getEnergyGoal();

}