package org.example.temperature.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.example.temperature.api.TemperatureConfiguration;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component
@Instantiate(name = "temperature.controller")
@Provides(specifications = { TemperatureConfiguration.class, PeriodicRunnable.class })
@SuppressWarnings("rawtypes")
public class TemperatureRegulatorImpl implements DeviceListener, PeriodicRunnable, TemperatureConfiguration {

	/**
	 * Temperature in Kelvin In icasa : Temperature min : 283 K Temperature max
	 * : 303 K
	 */
	private Map<String, Double> temperatureAndRoomAssociation;

	/**
	 * In Kelvin
	 */
	private static final double ABSOLUTE_ZERO = 0;

	/**
	 * Desired precision on the temperature Should not exceed 1
	 */
	private static final double PRECISION_OF_TEMPERATURE = 0.1;

	/**
	 * Authorized error between current temperature and targeted temperature for
	 * modification
	 */
	private static final double AUTHORIZED_ERROR = 0.5;

	/**
	 * Default temperature Icasa environment goes to
	 */
	private static final double DEFAULT_TEMPERATURE = 293;

	/**
	 * Definition of the period for the periodic runnable implementation
	 */
	private static final long PERIODIC_RUNNABLE_PERIOD = 10000;
	private static final TimeUnit PERIODIC_RUNNABLE_UNIT = TimeUnit.SECONDS;

	/**
	 * Is there a change of targeted temperature, so from setTargetedTemperature
	 */
	private String ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION = null;

	@Requires(id = "coolers", optional = true)
	private Cooler[] coolers;

	@Requires(id = "thermometers", optional = true)
	private Thermometer[] thermometers;

	@Requires(id = "heaters", optional = true)
	private Heater[] heaters;

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/*------------------------------BINDING METHODS--------------------------*/
	@Bind(id = "coolers")
	public synchronized void bindCooler(Cooler cooler, Map properties) {
		System.out.println("bind cooler " + cooler.getSerialNumber());
		cooler.addListener(this);
	}

	@Unbind(id = "coolers")
	public synchronized void unbindCooler(Cooler cooler, Map properties) {
		System.out.println("unbind cooler " + cooler.getSerialNumber());
		cooler.removeListener(this);
	}

	@Bind(id = "thermometers")
	public synchronized void bindThermometer(Thermometer thermometer, Map properties) {
		System.out.println("bind thermometer " + thermometer.getSerialNumber());
		thermometer.addListener(this);
	}

	@Unbind(id = "thermometers")
	public synchronized void unbindThermometer(Thermometer thermometer, Map properties) {
		System.out.println("Unbind thermometer " + thermometer.getSerialNumber());
		thermometer.removeListener(this);
	}

	@Bind(id = "heaters")
	public void bindHeater(Heater heater, Map properties) {
		System.out.println("bind heater " + heater.getSerialNumber());
		heater.addListener(this);
	}

	@Unbind(id = "heaters")
	public void unbindHeater(Heater heater, Map properties) {
		System.out.println("Unbind heater " + heater.getSerialNumber());
		heater.removeListener(this);
	}

	/*--------------------------------------COMPONENT LIFECYCLE--------------*/
	/** Component Lifecycle Method */
	@Invalidate
	public synchronized void stop() {
		System.out.println("Component is stopping...");
		for (Heater heater : heaters) {
			heater.removeListener(this);
		}
		for (Cooler cooler : coolers) {
			cooler.removeListener(this);
		}
		for (Thermometer thermometer : thermometers) {
			thermometer.removeListener(this);
		}
	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		System.out.println("Component is starting...");
		this.temperatureAndRoomAssociation = new HashMap<>();
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {

	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {
	}

	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

		if (device instanceof Thermometer) {

			if (propertyName.equals(Thermometer.LOCATION_PROPERTY_NAME)) {

				refreshTemperatureGoals();

				if (this.temperatureAndRoomAssociation.size() != 0) {

					/* List of current rooms discovered */
					for (Map.Entry mapentry : this.temperatureAndRoomAssociation.entrySet()) {
						System.out
								.println("Room: " + mapentry.getKey() + " | temperature goal: " + mapentry.getValue());
					}

				}

				if (getDeviceFromLocation((String) oldValue, thermometers, Thermometer.class).isEmpty()) {
					setStateHeatersCoolers((String) oldValue, 0);
				}

			} 
		}

		/*
		 * For heaters and coolers, nothing to do, thermometers take care of
		 * their behavior
		 */

	}

	/**
	 * powerToSetFromTemperature
	 * 
	 * @brief function which determines the power level for coolers and heaters
	 *        according to the difference between the current temperature and
	 *        the temperature goal
	 * @param currentTemperature
	 * @param temperatureGoal
	 * @return the signed power level
	 */
	private double powerToSetFromTemperature(double currentTemperature, double temperatureGoal) {

		double power = temperatureGoal - currentTemperature;

		while (Math.abs(power) > 1d) {
			power = ((Math.abs(power) <= PRECISION_OF_TEMPERATURE) ? 0 : (power / 10));
		}

		return power;
	}

	/**
	 * setStateHeatersCoolers
	 * 
	 * @brief if powerLevelToSet is negative, set coolers to decrease
	 *        temperature if powerLevelToSet is positive, set heaters to
	 *        increase temperature if powerLevelToSet is equal to zero, turns
	 *        off coolers and heaters
	 * @param location
	 * @param powerLevelToSet
	 */
	private void setStateHeatersCoolers(String location, double powerLevelToSet) {

		if (powerLevelToSet > 0) {
			setHeaterStateFromLocation(location, powerLevelToSet);
			setCoolerStateFromLocation(location, 0);
		} else if (powerLevelToSet < 0) {
			setHeaterStateFromLocation(location, 0);
			setCoolerStateFromLocation(location, -powerLevelToSet);
		} else {
			setHeaterStateFromLocation(location, 0);
			setCoolerStateFromLocation(location, 0);
		}
	}

	private void setHeaterStateFromLocation(String location, double powerLevelToSet) {

		List<Heater> sameLocationHeaters = getDeviceFromLocation(location, heaters, Heater.class);

		for (Heater heater : sameLocationHeaters) {
			heater.setPowerLevel(powerLevelToSet);
		}

	}

	private void setCoolerStateFromLocation(String location, double powerLevelToSet) {

		List<Cooler> sameLocationCoolers = getDeviceFromLocation(location, coolers, Cooler.class);

		for (Cooler cooler : sameLocationCoolers) {
			cooler.setPowerLevel(powerLevelToSet);
		}
	}

	private void refreshTemperatureGoals() {

		for (int i = 0; i < thermometersLocations().size(); i++) {
			if ((this.temperatureAndRoomAssociation.get(thermometersLocations().get(i)) == null)
					&& (!thermometersLocations().get(i).equals(Thermometer.LOCATION_UNKNOWN))) {
				this.temperatureAndRoomAssociation.put(thermometersLocations().get(i), DEFAULT_TEMPERATURE);
				System.out.println("Adding room: " + thermometersLocations().get(i));

			}
		}
	}

	private List<String> thermometersLocations() {
		Boolean alreadyInLocations;
		List<String> locations = new ArrayList<String>();
		for (int i = 0; i < thermometers.length; i++) {
			if (locations.isEmpty())
				locations.add(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString());
			else {
				alreadyInLocations = false;
				for (int j = 0; j < locations.size(); j++) {

					if (locations.get(j).equals(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString()))
						alreadyInLocations = true;
				}
				if (!alreadyInLocations)
					locations.add(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString());
			}
		}

		return locations;
	}

	private double averageTemperature(String location) {

		double averageTemp;
		averageTemp = 0;

		List<Thermometer> therm = getDeviceFromLocation(location, thermometers, Thermometer.class);

		for (int i = 0; i < therm.size(); i++) {
			averageTemp += therm.get(i).getTemperature();
		}

		return averageTemp / therm.size();

	}

	public void setTargetedTemperature(String targetedRoom, float temperature) {
		if (!targetedRoom.equals(Thermometer.LOCATION_UNKNOWN)) {
			if (this.temperatureAndRoomAssociation.get(targetedRoom) != null) {

				if ((this.temperatureAndRoomAssociation.get(targetedRoom)
						- averageTemperature(targetedRoom)) < AUTHORIZED_ERROR) {
					this.temperatureAndRoomAssociation.replace(targetedRoom, (double) temperature);
					this.ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION = targetedRoom;
				} else
					System.out.println("Previous temperature target still not reached");

			} else
				System.out.println("Room unknown");
		}
	}

	public float getTargetedTemperature(String room) {

		if (!room.equals(Thermometer.LOCATION_UNKNOWN) && (this.temperatureAndRoomAssociation.get(room) != null)) {

			return this.temperatureAndRoomAssociation.get(room).floatValue();
		} else {
			System.out.println("Room unknown");
			return 0;
		}
	}

	/**
	 * getDeviceFromLocation
	 * 
	 * @param location
	 * @param genericDevices
	 * @param clazz
	 * @return liste de type T passe en parametre Fonction BOGOSS
	 */
	private synchronized <T extends GenericDevice> List<T> getDeviceFromLocation(String location, T[] genericDevices,
			Class<T> clazz) {
		List<T> deviceLocation = new ArrayList<>();
		for (T genericDev : genericDevices) {
			if (genericDev.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				deviceLocation.add(genericDev);
			}
		}
		return deviceLocation;
	}

	/*--------------------Periodic Runnable implementation-----------*/

	@Override
	public void run() {

		if (this.ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION != null) {
			if (averageTemperature(ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION) >= ABSOLUTE_ZERO)
				setStateHeatersCoolers(ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION, powerToSetFromTemperature(
						averageTemperature(ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION),
						this.temperatureAndRoomAssociation.get(ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION)));
			this.ANY_CHANGE_ON_TARGETED_TEMPERATURE_OF_LOCATION = null;
		}

		refreshTemperatureGoals();

		for (Map.Entry mapentry : this.temperatureAndRoomAssociation.entrySet()) {

			if (averageTemperature((String) mapentry.getKey()) >= ABSOLUTE_ZERO)
				setStateHeatersCoolers((String) mapentry.getKey(), powerToSetFromTemperature(
						averageTemperature((String) mapentry.getKey()), (double) mapentry.getValue()));

			System.out.println("Room: " + mapentry.getKey() + " | temperature goal: " + mapentry.getValue());
		}
	

	}

	/**
	 * Return the lenght of the period between each action of Periodic Runnable
	 */
	@Override
	public long getPeriod() {
		return PERIODIC_RUNNABLE_PERIOD;
	}

	/**
	 * Return the unit of the period between each action of Periodic Runnable
	 */
	@Override
	public TimeUnit getUnit() {
		return PERIODIC_RUNNABLE_UNIT;
	}

}
