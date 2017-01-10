package org.example.temperature.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.temperature.api.TemperatureConfiguration;
import org.example.temperature.api.TemperatureManagerAdministration;

@Component
@Instantiate(name = "temperature.manager")
@Provides(specifications = { TemperatureManagerAdministration.class })
public class TemperatureManagerImpl implements TemperatureManagerAdministration {

	@Requires
	private TemperatureConfiguration temperatureManager;

	/**
	 * Accessible range in icasa
	 */
	private static final double MIN_TEMPERATURE = 283;
	private static final double MAX_TEMPERATURE = 303;

	public void temperatureIsTooHigh(String roomName) {

		if (!(temperatureManager.getTargetedTemperature(roomName) == 0)) {
			if (Math.floor(temperatureManager.getTargetedTemperature(roomName)) > MIN_TEMPERATURE) {
				temperatureManager.setTargetedTemperature(roomName,
						temperatureManager.getTargetedTemperature(roomName) - 1);
			} else
				System.out.println("Min temperature reached in " + roomName);
		}

	}

	public void temperatureIsTooLow(String roomName) {

		if (!(temperatureManager.getTargetedTemperature(roomName) == 0)) {
			if (Math.ceil(temperatureManager.getTargetedTemperature(roomName)) < MAX_TEMPERATURE) {
				temperatureManager.setTargetedTemperature(roomName,
						temperatureManager.getTargetedTemperature(roomName) + 1);
			} else
				System.out.println("Max temperature reached in " + roomName);
		}
	}

}
