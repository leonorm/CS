package org.example.follow.me.regulator;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.example.follow.me.api.FollowMeConfiguration;

@Component
@Instantiate(name = "light.follow.me.regulator")
@Provides(specifications={FollowMeConfiguration.class})
@SuppressWarnings("rawtypes")
/**
 * Created by aygalinc on 28/10/16.
 */
public class LightFollowMeRegulatorImpl implements DeviceListener, FollowMeConfiguration{

	/**
	 * The maximum number of lights to turn on when a user enters the room :
	 **/
	private int maxLightsToTurnOnPerRoom = 2;

	@Requires(id="presenceSensors", optional=true)
	/** Field for presenceSensors dependency */
	private PresenceSensor[] presenceSensors;
	@Requires(id="binaryLights", optional=true)
	/** Field for binaryLights dependency */
	private BinaryLight[] binaryLights;
	@Requires(id="dimmerLights", optional=true)
	/** Field for dimmerLights dependency */
	private DimmerLight[] dimmerLights;

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/*------------------------------BINDING METHODS--------------------------*/
	@Bind(id="binaryLights")
	/** Bind Method for binaryLights dependency */
	public synchronized void bindBinaryLight(BinaryLight binaryLight, Map properties) {
		System.out.println("bind binary light " + binaryLight.getSerialNumber());
		binaryLight.addListener(this);
	}
	@Unbind(id="binaryLights")
	/** Unbind Method for binaryLights dependency */
	public synchronized void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
		System.out.println("unbind binary light " + binaryLight.getSerialNumber());
		binaryLight.removeListener(this);
	}
	@Bind(id="presenceSensors")
	/** Bind Method for presenceSensors dependency */
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.addListener(this);
	}
	@Unbind(id="presenceSensors")
	/** Unbind Method for presenceSensors dependency */
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("Unbind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.removeListener(this);
	}
	@Bind(id="dimmerLights")
	/** Bind Method for dimmerLights dependency */
	public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
		System.out.println("bind dimmer light" + dimmerLight.getSerialNumber());
		dimmerLight.addListener(this);
	}
	@Unbind(id="dimmerLights")
	/** Unbind Method for dimmerLights dependency */
	public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
		System.out.println("Unbind dimmer light " + dimmerLight.getSerialNumber());
		dimmerLight.removeListener(this);
	}

	/*--------------------------------------COMPONENT LIFECYCLE--------------*/
	/** Component Lifecycle Method */
	@Invalidate
	public synchronized void stop() {
		System.out.println("Component is stopping...");
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
		for (BinaryLight binaryLight : binaryLights) {
			binaryLight.removeListener(this);
		}
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

	/**
	 * PRESENCE This method is part of the DeviceListener interface and is
	 * called when a subscribed device property is modified.
	 * 
	 * @param device
	 *            is the device whose property has been modified.
	 * @param propertyName
	 *            is the name of the modified property.
	 * @param oldValue
	 *            is the old value of the property
	 * @param newValue
	 *            is the new value of the property
	 */
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

		/*
		 * If the device is a presence sensor
		 */
		if (device instanceof PresenceSensor) {

			PresenceSensor changingSensor = (PresenceSensor) device;

			/*
			 * Sensivity property of the presence sensor
			 */
			if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {

				if (!changingSensor.getPropertyValue(PresenceSensor.LOCATION_PROPERTY_NAME)
						.equals(PresenceSensor.LOCATION_UNKNOWN)) {
					if (changingSensor.getSensedPresence())
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME),
								maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME), 0);
				}
			}

			/*
			 * Location property of the presence sensor
			 */
			else if (propertyName.equals(PresenceSensor.LOCATION_PROPERTY_NAME)) {

				/* if the location is known */
				if (!changingSensor.getPropertyValue(PresenceSensor.LOCATION_PROPERTY_NAME)
						.equals(PresenceSensor.LOCATION_UNKNOWN)) {
					/* if a presence is sensed */
					if (changingSensor.getSensedPresence())
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME),
								maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME), 0);
				}
				/*
				 * turn off lights in the previous location is there is no
				 * presence sensor
				 */
				if (getDeviceFromLocation((String) oldValue, presenceSensors, PresenceSensor.class).isEmpty()) {
					setStateLights((String) oldValue, 0);
				}
			}

		}

		/*
		 * If the device is a Binary Light
		 */
		else if (device instanceof BinaryLight) {

			BinaryLight lightBulb = (BinaryLight) device;

			/* check if the change is related to the light location */
			if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)) {

				/* if the new location is unknown */
				if (newValue.equals(BinaryLight.LOCATION_UNKNOWN)) {

					lightBulb.turnOff();

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) oldValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) oldValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) oldValue, 0);
				}

				else {

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) oldValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) oldValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) oldValue, 0);

					/* Refresh in the new location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) newValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) newValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) newValue, 0);

				}

			}
		} else if (device instanceof DimmerLight) {

			DimmerLight lightBulb = (DimmerLight) device;

			/* check if the change is related to the light location */
			if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)) {

				/* if the new location is unknown */
				if (newValue.equals(DimmerLight.LOCATION_UNKNOWN)) {

					lightBulb.setPowerLevel(0.0);

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) oldValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) oldValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) oldValue, 0);
				}

				else {

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) oldValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) oldValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) oldValue, 0);

					/* Refresh in the new location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getDeviceFromLocation((String) newValue, presenceSensors, PresenceSensor.class)))
						setStateLights((String) newValue, maxLightsToTurnOnPerRoom);
					else
						setStateLights((String) newValue, 0);

				}

			}
		}

	}

	private boolean isThereAPresenceSensed(List<PresenceSensor> presenceSensors) {
		for (PresenceSensor p : presenceSensors) {
			if (p.getSensedPresence())
				return true;
		}
		return false;
	}

	/*----------------------------------LAMPS MANAGER ---------------------------*/
	private void setStateLights(String location, int maxNumberOfLightedLamps) {

		int numberOfLightedLamps;
		numberOfLightedLamps = setStateDimmerLightFromLocation(location, maxNumberOfLightedLamps);
		setStateBinaryLightFromLocation(location, maxNumberOfLightedLamps - numberOfLightedLamps);

	}

	private int setStateBinaryLightFromLocation(String location, int maxNumberOfLightedLamps) {

		/* list of binary lights in the location */
		List<BinaryLight> sameLocationLigths = getDeviceFromLocation(location, binaryLights, BinaryLight.class);

		int numberOfLightedLamps = getNumberOfLightedLamps(sameLocationLigths);

		for (BinaryLight binaryLight : sameLocationLigths) {
			/*
			 * switch them on/off depending on the number of lighted lamps
			 * and their power status
			 */
			if (numberOfLightedLamps < maxNumberOfLightedLamps) {
				if (!binaryLight.getPowerStatus()) {
					binaryLight.turnOn();
					numberOfLightedLamps++;
				}
			} else if (numberOfLightedLamps > maxNumberOfLightedLamps) {
				if (binaryLight.getPowerStatus()) {
					binaryLight.turnOff();
					numberOfLightedLamps--;
				}
			}
		}
		return numberOfLightedLamps;
	}

	private int setStateDimmerLightFromLocation(String location, int maxNumberOfLightedLamps) {

		/* list of binary lights in the location */
		List<DimmerLight> sameLocationLigths = getDeviceFromLocation(location, dimmerLights, DimmerLight.class);

		int numberOfLightedDimmerLamps = getNumberOfLightedDimmerLamps(sameLocationLigths);

		for (DimmerLight dimmerLight : sameLocationLigths) {
			/*
			 * switch them on/off depending on the number of lighted lamps
			 * and their power status
			 */
			if (numberOfLightedDimmerLamps < maxNumberOfLightedLamps) {
				if (dimmerLight.getPowerLevel() == 0.0) {
					dimmerLight.setPowerLevel(1.0);
					numberOfLightedDimmerLamps++;
				}
			} else if (numberOfLightedDimmerLamps > maxNumberOfLightedLamps) {
				if (dimmerLight.getPowerLevel() > 0.0) {
					dimmerLight.setPowerLevel(0.0);
					numberOfLightedDimmerLamps--;
				}
			}
		}
		return numberOfLightedDimmerLamps;
	}

	private synchronized int getNumberOfLightedLamps(List<BinaryLight> listOfBinaryLights) {

		int counter = 0;

		for (BinaryLight binaryLight : listOfBinaryLights) {
			if (binaryLight.getPowerStatus())
				counter++;
		}

		return counter;
	}

	private synchronized int getNumberOfLightedDimmerLamps(List<DimmerLight> listOfDimmerLights) {

		int counter = 0;

		for (DimmerLight dimmerLight : listOfDimmerLights) {
			if (dimmerLight.getPowerLevel() > 0)
				counter++;
		}

		return counter;
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

	/*----------------------------------FollowMe Configuration methods ------*/

	/**
	 * Gets the maximum number of lights to turn on each time an user is
	 * entering a room.
	 * 
	 * @return the maximum number of lights to turn on
	 */
	public int getMaximumNumberOfLightsToTurnOn() {
		
		return this.maxLightsToTurnOnPerRoom;
	}

	/**
	 * Sets the maximum number of lights to turn on each time an user is
	 * entering a room.
	 * 
	 * @param maximumNumberOfLightsToTurnOn
	 *            the new maximum number of lights to turn on
	 */
	public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		this.maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
	}

}
