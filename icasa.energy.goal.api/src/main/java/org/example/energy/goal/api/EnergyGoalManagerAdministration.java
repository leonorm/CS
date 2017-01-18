package org.example.energy.goal.api;

import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.IlluminanceGoal;

public interface EnergyGoalManagerAdministration {
 
	
	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal);

	public IlluminanceGoal getIlluminancePreference();

	public void setEnergySavingGoal(EnergyGoal energyGoal);

	public EnergyGoal getEnergyGoal();
	
	public void temperatureIsTooHigh(String roomName);
	 
    public void temperatureIsTooLow(String roomName);

    public void setEnergyManagementGoal(EnergyManagementGoal energyGoal, String roomName);

    public EnergyManagementGoal getEnergyManagementGoal();
    
}