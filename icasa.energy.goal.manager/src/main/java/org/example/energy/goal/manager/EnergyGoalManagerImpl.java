package org.example.energy.goal.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.energy.goal.api.EnergyGoalManagerAdministration;
import org.example.energy.goal.api.EnergyManagementGoal;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.IlluminanceGoal;
import org.example.temperature.api.TemperatureManagerAdministration;

@Component
@Instantiate(name = "energy.goal.manager")
@Provides(specifications = { EnergyGoalManagerAdministration.class })
public class EnergyGoalManagerImpl implements EnergyGoalManagerAdministration {

	/** Field for followMeManager dependency */
	@Requires
	private FollowMeAdministration followMeManager;
	
	@Requires
	private TemperatureManagerAdministration temperatureManager;
	
	private EnergyManagementGoal energyGoal = EnergyManagementGoal.FULL;
	
	private final static double MAX_TEMPERATURE_MEDIUM = 294.15;
	
	private final static double MAX_TEMPERATURE_SOFT = 291.15;
	
	
	
	
	
	public void setEnergyManagementGoal(EnergyManagementGoal energyGoal, String roomName) {

		if (EnergyManagementGoal.SOFT.equals(energyGoal)) {
			this.energyGoal = EnergyManagementGoal.SOFT;
			
		} else if (EnergyManagementGoal.MEDIUM.equals(energyGoal)) {
			this.energyGoal = EnergyManagementGoal.MEDIUM;
			
		} else 
			this.energyGoal = EnergyManagementGoal.FULL;
		
		goalCases(roomName, true, null, null);
	}
	
	
	
	public void temperatureIsTooHigh(String roomName){
		goalCases(roomName, false, null, null);
	}
	 
    public void temperatureIsTooLow(String roomName){
    	goalCases(roomName, true, null, null);
    }
	
	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		goalCases(null, null, illuminanceGoal, null);
	}


	public void setEnergySavingGoal(EnergyGoal energyGoal) {
		goalCases(null, null, null, energyGoal);
	}
	

	/***
	 * 
	 * @param roomName
	 * @param tooHighTooLow true if too Low and false if too high
	 * @param illuminanceGoal
	 * @param energyGoal
	 */
	private void goalCases(String roomName, Boolean tooHighTooLow, IlluminanceGoal illuminanceGoal, EnergyGoal energyGoal){
		
		if(this.energyGoal.equals(EnergyManagementGoal.SOFT)){
			
			if(roomName != null){
				if(tooHighTooLow){
					if(temperatureManager.getTemperature(roomName) < MAX_TEMPERATURE_SOFT){
						temperatureManager.temperatureIsTooLow(roomName);
						followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
					}
					else if(temperatureManager.getTemperature(roomName) > MAX_TEMPERATURE_SOFT){
						System.out.println("Baisse de la température car trop élevée");
						temperatureManager.temperatureIsTooHigh(roomName);
						followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
					}
				}
				else{
					temperatureManager.temperatureIsTooHigh(roomName);
					followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
				}
				 	
			}else if(illuminanceGoal != null){
					followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
					if(temperatureManager.getTemperature(roomName) > MAX_TEMPERATURE_SOFT){
						System.out.println("Baisse de la température car trop élevée");
						temperatureManager.temperatureIsTooHigh(roomName);
					}
				followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
			}else{	
				followMeManager.setEnergySavingGoal(EnergyGoal.LOW);
				if(temperatureManager.getTemperature(roomName) > MAX_TEMPERATURE_SOFT){
					System.out.println("Baisse de la température car trop élevée");
					temperatureManager.temperatureIsTooHigh(roomName);
				}
			}
		}else if(this.energyGoal.equals(EnergyManagementGoal.MEDIUM)){
			if(roomName != null){
				
				if(tooHighTooLow){
					if(IlluminanceGoal.SOFT.equals(followMeManager.getIlluminancePreference()) && temperatureManager.getTemperature(roomName) < MAX_TEMPERATURE_MEDIUM ){
						temperatureManager.temperatureIsTooLow(roomName);
					}
					else if(temperatureManager.getTemperature(roomName) > MAX_TEMPERATURE_MEDIUM){
						System.out.println("Baisse de la température car trop élevée");
						temperatureManager.temperatureIsTooHigh(roomName);
					}
				}else{
					temperatureManager.temperatureIsTooHigh(roomName);
					followMeManager.setIlluminancePreference(IlluminanceGoal.MEDIUM);
				}
				
				
				
			}else if(illuminanceGoal != null){

				if(IlluminanceGoal.SOFT.equals(illuminanceGoal)){
					followMeManager.setIlluminancePreference(IlluminanceGoal.SOFT);
					/*it would be necessary to set the illuminance goal by room, so make changes in regulator and manager of follow me*/
				}else if (IlluminanceGoal.MEDIUM.equals(illuminanceGoal)){
					followMeManager.setIlluminancePreference(IlluminanceGoal.MEDIUM);
					/*it would be necessary to set the illuminance goal by room, so make changes in regulator and manager of follow me*/
				}else{
					followMeManager.setIlluminancePreference(IlluminanceGoal.MEDIUM);
					/*it would be necessary to set the illuminance goal by room, so make changes in regulator and manager of follow me*/
				}
				
			}else{	
				followMeManager.setEnergySavingGoal(EnergyGoal.MEDIUM);
			}
		}else if(this.energyGoal.equals(EnergyManagementGoal.FULL)){
			if(roomName != null){	
				if(tooHighTooLow)
					temperatureManager.temperatureIsTooLow(roomName);
				else
					temperatureManager.temperatureIsTooHigh(roomName);
			}else if(illuminanceGoal != null){
				followMeManager.setIlluminancePreference(illuminanceGoal);
			}else{	
				followMeManager.setEnergySavingGoal(energyGoal);	
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public IlluminanceGoal getIlluminancePreference() {
		return followMeManager.getIlluminancePreference();
	}
	
	

	public EnergyManagementGoal getEnergyManagementGoal() {

		System.out.println("But énergétique actuel :" + this.energyGoal);
		return this.energyGoal;
		
	}

	
	public EnergyGoal getEnergyGoal() {
		return followMeManager.getEnergyGoal();
	}	
	
	
	
	
	
	
	
	
	
	
	

}
