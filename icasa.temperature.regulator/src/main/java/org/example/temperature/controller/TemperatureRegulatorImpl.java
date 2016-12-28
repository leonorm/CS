package org.example.temperature.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

@Component
@Instantiate(name = "temperature.controller")
@SuppressWarnings("rawtypes")
public class TemperatureRegulatorImpl implements DeviceListener, Runnable {

	/**
	 * Temperature in Kelvin
	 */
	private double temperatureGoal = 285.15;
	
	private final Lock _mutex = new ReentrantLock(true);
	
//	private Map<String , Double> temperatureAndRoomAssociation;
	
	private volatile Thread thread;
	
	/*Delay between two temperature measures*/
	private static final int DELAY = 1000;

	private static final double ABSOLUTE_ZERO= 273.15;
	
	private double Temperature;
	
	
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
		thread = new Thread(this);
		thread.start();
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
		this.thread=null;
	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		System.out.println("Component is starting...");
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
		
			if(device instanceof Thermometer){
				
				Thermometer temperatureSensor = (Thermometer) device; 
				
				if (propertyName.equals(Thermometer.THERMOMETER_CURRENT_TEMPERATURE)) {
					_mutex.lock();
					if(averageTemperature((String) temperatureSensor.getPropertyValue(LOCATION_PROPERTY_NAME)) >= ABSOLUTE_ZERO)
						setStateHeatersCoolers((String) temperatureSensor.getPropertyValue(LOCATION_PROPERTY_NAME), powerToSetFromTemperature(averageTemperature((String) temperatureSensor.getPropertyValue(LOCATION_PROPERTY_NAME)), temperatureGoal) );
					System.out.println("temperature in  " + (String) temperatureSensor.getPropertyValue(LOCATION_PROPERTY_NAME)  +" "+ temperatureSensor.getTemperature());
					_mutex.unlock();
				}
				
				
				
		}
	}
	
	
	
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread == this.thread) {
			try {

				_mutex.lock();
				Thread.sleep(DELAY);
				_mutex.unlock();
				if(thermometersLocations().isEmpty())
					this.thread = null;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * powerToSetFromTemperature
	 * @brief function which determines the power level for coolers and heaters according 
	 * 		  to the difference between the current temperature and the temperature goal
	 * @param currentTemperature
	 * @param temperatureGoal
	 * @return the power level 
	 */
	private double powerToSetFromTemperature(double currentTemperature, double temperatureGoal){
		
		/* the physical temperature model could be implemented here */ 
		return ((temperatureGoal - currentTemperature)/100d);
	}
	
	
	
	/**
	 * setStateHeatersCoolers
	 * 
	 * @brief if powerLevelToSet is negative, set coolers to decrease temperature
	 * 		  if powerLevelToSet is positive, set heaters to increase temperature
	 * 	      if powerLevelToSet is equal to zero, turns off coolers and heaters
	 * @param location
	 * @param powerLevelToSet
	 */
	private void setStateHeatersCoolers(String location, double powerLevelToSet) {

		if(powerLevelToSet > 0 ){
			setHeaterStateFromLocation(location, powerLevelToSet);
			setCoolerStateFromLocation(location, 0);
		}else if(powerLevelToSet < 0 ){
			setHeaterStateFromLocation(location, 0);
			setCoolerStateFromLocation(location, -powerLevelToSet);
		}else{
			setHeaterStateFromLocation(location, 0);
			setCoolerStateFromLocation(location, 0);
		}
	}
	
	private void setHeaterStateFromLocation(String location, double powerLevelToSet){
		
		List<Heater> sameLocationHeaters = getDeviceFromLocation(location, heaters, Heater.class);

		for (Heater heater : sameLocationHeaters) {
			heater.setPowerLevel(powerLevelToSet);
		}
		
	}

	private void setCoolerStateFromLocation(String location, double powerLevelToSet){
		
		List<Cooler> sameLocationCoolers = getDeviceFromLocation(location, coolers, Cooler.class);

		for (Cooler cooler : sameLocationCoolers) {
			cooler.setPowerLevel(powerLevelToSet);
		}
	}
	
	private List<String> thermometersLocations(){
		Boolean alreadyInLocations;
		List<String> locations = new ArrayList<String>();
		for(int i=0; i<thermometers.length; i++ ){
			if(locations.isEmpty())
				locations.add(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString());
			else{
				alreadyInLocations = false;
				for(int j=0; j< locations.size(); j++){
					
					if(locations.get(j).equals(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString()))
						alreadyInLocations = true;
				}
				if(!alreadyInLocations)
					locations.add(thermometers[i].getPropertyValue(LOCATION_PROPERTY_NAME).toString());
			}
		}
		
		return locations;
	}
	
	private double averageTemperature(String location){
		
		double averageTemp;
		averageTemp = 0;
		
		List<Thermometer> therm= getDeviceFromLocation(location, thermometers, Thermometer.class);
			
		for(int i=0; i < therm.size(); i++){
			averageTemp += therm.get(i).getTemperature();
		}
		
		return averageTemp/therm.size();

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
	
}
