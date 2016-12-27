package org.example.follow.me.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.FollowMeConfiguration;
import org.example.follow.me.api.IlluminanceGoal;

/**
 * Created by aygalinc on 28/10/16.
 */

@Component
@Instantiate(name = "light.follow.me.manager")
@Provides(specifications = { FollowMeAdministration.class })
public class LightFollowMeManagerImpl implements FollowMeAdministration {

	/** Field for followMeManager dependency */
	@Requires
	private FollowMeConfiguration followMeManager;

	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {

		if (IlluminanceGoal.SOFT.equals(illuminanceGoal)) {
			followMeManager.setMaximumNumberOfLightsToTurnOn(1);
		} else if (IlluminanceGoal.MEDIUM.equals(illuminanceGoal)) {
			followMeManager.setMaximumNumberOfLightsToTurnOn(2);
		} else if (IlluminanceGoal.FULL.equals(illuminanceGoal)) {
			followMeManager.setMaximumNumberOfLightsToTurnOn(3);
		}
	}

	public IlluminanceGoal getIlluminancePreference() {

		System.out.println("nombre de lampes a allumer :" + followMeManager.getMaximumNumberOfLightsToTurnOn());

		switch (followMeManager.getMaximumNumberOfLightsToTurnOn()) {
		case 1:
			return IlluminanceGoal.SOFT;
		case 2:
			return IlluminanceGoal.MEDIUM;
		case 3:
			return IlluminanceGoal.FULL;
		default:
			return null;
		}
	}

	public void setEnergySavingGoal(EnergyGoal energyGoal) {
		if (EnergyGoal.LOW.equals(energyGoal)) {
			followMeManager.setMaximumAllowedEnergyInRoom(100d);
		} else if (EnergyGoal.MEDIUM.equals(energyGoal)) {
			followMeManager.setMaximumAllowedEnergyInRoom(200d);
		} else if (EnergyGoal.HIGH.equals(energyGoal)) {
			followMeManager.setMaximumAllowedEnergyInRoom(1000d);
		}

	}

	public EnergyGoal getEnergyGoal() {
		
		System.out.println("Energie max par piece :" + followMeManager.getMaximumAllowedEnergyInRoom());

		if(followMeManager.getMaximumAllowedEnergyInRoom() == 100d) 
			return EnergyGoal.LOW;
		else if(followMeManager.getMaximumAllowedEnergyInRoom() == 200d) 
			return EnergyGoal.MEDIUM;
		else if(followMeManager.getMaximumAllowedEnergyInRoom() == 1000d) 
			return EnergyGoal.HIGH;
		else
			return null;
	}

}
