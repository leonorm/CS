package org.example.temperature.api;

/**
 * The TemperatureConfiguration service allows one to configure the temperature
 * controller.
 */
public interface TemperatureConfiguration {

	/**
	 * Configure the controller to reach a given temperature in Kelvin in a
	 * given room.
	 * 
	 * @param targetedRoom
	 *            the targeted room name
	 * @param temperature
	 *            the temperature in Kelvin (>=0)
	 */
	public void setTargetedTemperature(String targetedRoom, float temperature);

	/**
	 * Gets the targetted temperature of a given room.
	 * 
	 * @param room
	 *            the room name
	 * @return the temperature in Kelvin
	 */
	public float getTargetedTemperature(String room);

}