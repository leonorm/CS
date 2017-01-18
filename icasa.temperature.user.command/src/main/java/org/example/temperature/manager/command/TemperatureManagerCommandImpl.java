package org.example.temperature.manager.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.energy.goal.api.EnergyGoalManagerAdministration;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
 
@Component
@Instantiate(name = "temperature.user.command")
@CommandProvider(namespace = "temperatureCommand")
public class TemperatureManagerCommandImpl {
 
    @Requires
    private EnergyGoalManagerAdministration m_administrationService;
 
    @Command
    public void tempIsTooHigh(String room) {
        m_administrationService.temperatureIsTooHigh(room);
    }
 
    @Command
    public void tempIsTooLow(String room){
    	m_administrationService.temperatureIsTooLow(room);
    }    
}
