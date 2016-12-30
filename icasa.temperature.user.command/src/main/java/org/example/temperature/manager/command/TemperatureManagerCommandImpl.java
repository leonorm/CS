package org.example.temperature.manager.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.temperature.api.TemperatureManagerAdministration;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
 
@Component
@Instantiate(name = "temperature.command")
@CommandProvider(namespace = "temperature")
public class TemperatureManagerCommandImpl {
 
    @Requires
    private TemperatureManagerAdministration m_administrationService;
 
    @Command
    public void tempTooHigh(String room) {
        m_administrationService.temperatureIsTooHigh(room);
    }
 
    @Command
    public void tempTooLow(String room){
    	m_administrationService.temperatureIsTooLow(room);
    }    
}
