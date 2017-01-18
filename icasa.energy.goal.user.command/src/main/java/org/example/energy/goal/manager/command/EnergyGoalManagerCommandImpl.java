package org.example.energy.goal.manager.command;

import java.rmi.UnexpectedException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.energy.goal.api.EnergyGoalManagerAdministration;
import org.example.energy.goal.api.EnergyManagementGoal;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
 
@Component
@Instantiate(name = "energy.command")
@CommandProvider(namespace = "Energy")
public class EnergyGoalManagerCommandImpl {
 
    @Requires
    private EnergyGoalManagerAdministration m_administrationService;
 
    @Command
    public void setEnergyTarget(String goal, String roomName) {
    	
    	EnergyManagementGoal energyGoal = null;

    	if(roomName != null){
	    	try
		    {	
		    	if(goal.equals("SOFT"))
		    		energyGoal = EnergyManagementGoal.SOFT;
		    	else if(goal.equals("MEDIUM"))
		    		energyGoal = EnergyManagementGoal.MEDIUM;
		    	else if(goal.equals("FULL"))
		    		energyGoal = EnergyManagementGoal.FULL;
		    	else{
		    		throw new UnexpectedException("Energy goal UNKWNOWN");
		    	}
		    }catch(UnexpectedException e){
		    	e.printStackTrace();
		    }
	    	
	    	if(energyGoal != null){
	    		m_administrationService.setEnergyManagementGoal(energyGoal, roomName);
	    	}
    	}
    }
 
    @Command
    public void getEnergyManagementGoal(){
    	m_administrationService.getEnergyManagementGoal();
    }    
    
    
}
