package org.example.follow.me.manager.command;

import java.rmi.UnexpectedException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.energy.goal.api.EnergyGoalManagerAdministration;
import org.example.follow.me.api.EnergyGoal;
import org.example.follow.me.api.IlluminanceGoal;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
 
//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "follow.me.command")
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")
public class FollowMeManagerCommandImpl {
 
    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private EnergyGoalManagerAdministration m_administrationService;
 
 
    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */
 
    // Each command should start with a @Command annotation
    @Command
    public void setIlluminancePreference(String goal) {
        // The targeted goal
        IlluminanceGoal illuminanceGoal = null;
 
        try
        {
	    	if(goal.equals("SOFT"))
	        	illuminanceGoal=IlluminanceGoal.SOFT;
	        else if(goal.equals("MEDIUM"))
	        	illuminanceGoal=IlluminanceGoal.MEDIUM;
	    	else if(goal.equals("FULL"))
	    		illuminanceGoal=IlluminanceGoal.FULL;
	    	else{
	    		throw new UnexpectedException("Illuminance goal UNKWNOWN");
	    		
	    	}
        }catch(UnexpectedException e){
        	e.printStackTrace();
        }
    	if(illuminanceGoal != null){
    		m_administrationService.setIlluminancePreference(illuminanceGoal);
    	}
    }
 
    @Command
    public void getIlluminancePreference(){
        System.out.println("The illuminance goal is "+ this.m_administrationService.getIlluminancePreference()); //...
    }

    @Command
    public void setEnergyPreference(String goal) {
        // The targeted goal
        EnergyGoal energyGoal = null;
 
        try
        {
	    	if(goal.equals("LOW"))
	    		energyGoal =EnergyGoal.LOW;
	        else if(goal.equals("MEDIUM"))
	        	energyGoal =EnergyGoal.MEDIUM;
	    	else if(goal.equals("HIGH"))
	    		energyGoal =EnergyGoal.HIGH;
	    	else{
	    		throw new UnexpectedException("Energy goal UNKWNOWN");
	    		
	    	}
        }catch(UnexpectedException e){
        	e.printStackTrace();
        }
    	if(energyGoal != null){
    		m_administrationService.setEnergySavingGoal(energyGoal);
    	}
    }
 
    @Command
    public void getEnergyPreference(){
        System.out.println("The energy goal is "+ this.m_administrationService.getEnergyGoal()); 
    }
    
}
