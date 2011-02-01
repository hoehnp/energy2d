package org.concord.energy2d.system;

import java.awt.Rectangle;
import java.util.List;

import org.concord.energy2d.model.Constants;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;

/**
 * @author Charles Xie
 * 
 */
class XmlEncoder {

	private System2D box;

	XmlEncoder(System2D box) {
		this.box = box;
	}

	String encode() {

		StringBuffer sb = new StringBuffer(1000);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<state>\n");

		// model properties

		sb.append("<model>\n");

		if (box.model.getLx() != 10) {
			sb.append("<model_width>" + box.model.getLx() + "</model_width>\n");
		}
		if (box.model.getLy() != 10) {
			sb.append("<model_height>" + box.model.getLy() + "</model_height>\n");
		}
		if (box.model.getTimeStep() != 1) {
			sb.append("<timestep>" + box.model.getTimeStep() + "</timestep>\n");
		}
		if (box.model.getMeasurementInterval() != 500) {
			sb.append("<measurement_interval>" + box.model.getMeasurementInterval()
					+ "</measurement_interval>\n");
		}
		if (box.model.getViewUpdateInterval() != 100) {
			sb.append("<viewupdate_interval>" + box.model.getViewUpdateInterval()
					+ "</viewupdate_interval>\n");
		}

		if (box.model.isSunny()) {
			sb.append("<sunny>true</sunny>");
		}
		sb.append("<sun_angle>" + box.model.getSunAngle() + "</sun_angle>\n");
		sb.append("<solar_power_density>" + box.model.getSolarPowerDensity()
				+ "</solar_power_density>\n");
		sb.append("<solar_ray_count>" + box.model.getSolarRayCount() + "</solar_ray_count>\n");
		sb.append("<solar_ray_speed>" + box.model.getSolarRaySpeed() + "</solar_ray_speed>\n");
		sb.append("<photon_emission_interval>" + box.model.getPhotonEmissionInterval()
				+ "</photon_emission_interval>\n");

		if (!box.model.isConvective()) {
			sb.append("<convective>false</convective>\n");
		}
		if (box.model.getBackgroundConductivity() != Constants.AIR_THERMAL_CONDUCTIVITY) {
			sb.append("<background_conductivity>" + box.model.getBackgroundConductivity()
					+ "</background_conductivity>\n");
		}
		if (box.model.getBackgroundDensity() != Constants.AIR_DENSITY) {
			sb.append("<background_density>" + box.model.getBackgroundDensity()
					+ "</background_density>\n");
		}
		if (box.model.getBackgroundSpecificHeat() != Constants.AIR_SPECIFIC_HEAT) {
			sb.append("<background_specific_heat>" + box.model.getBackgroundSpecificHeat()
					+ "</background_specific_heat>\n");
		}
		if (box.model.getBackgroundTemperature() != 0) {
			sb.append("<background_temperature>" + box.model.getBackgroundTemperature()
					+ "</background_temperature>\n");
		}
		if (box.model.getBackgroundViscosity() != Constants.AIR_VISCOSITY) {
			sb.append("<background_viscosity>" + box.model.getBackgroundViscosity()
					+ "</background_viscosity>\n");
		}
		sb.append("<thermal_buoyancy>" + box.model.getThermalBuoyancy() + "</thermal_buoyancy>\n");
		sb.append("<buoyancy_approximation>" + box.model.getBuoyancyApproximation()
				+ "</buoyancy_approximation>\n");

		sb.append("<boundary>\n");
		sb.append(box.model.getHeatBoundary().toXml());
		sb.append("</boundary>\n");

		List<Part> parts = box.model.getParts();
		if (!parts.isEmpty()) {
			sb.append("<structure>\n");
			for (Part p : parts) {
				sb.append(p.toXml());
			}
			sb.append("</structure>\n");
		}

		sb.append("</model>\n");

		// sensors
		sb.append("<sensor>\n");
		List<Thermometer> thermometers = box.model.getThermometers();
		if (thermometers != null) {
			for (Thermometer t : thermometers) {
				sb.append(t.toXml() + "\n");
			}
		}
		sb.append("</sensor>\n");

		// view properties

		sb.append("<view>\n");
		if (box.view.isGridOn()) {
			sb.append("<grid>true</grid>\n");
		}
		if (box.view.isRulerOn()) {
			sb.append("<ruler>true</ruler>\n");
		}
		if (box.view.isIsothermOn()) {
			sb.append("<isotherm>true</isotherm>\n");
		}
		if (box.view.isRainbowOn()) {
			sb.append("<rainbow>true</rainbow>\n");
		}
		Rectangle rainbow = box.view.getRainbowRectangle();
		sb.append("<rainbow_x>" + rainbow.x + "</rainbow_x>");
		sb.append("<rainbow_y>" + rainbow.y + "</rainbow_y>");
		if (rainbow.width > 0)
			sb.append("<rainbow_w>" + rainbow.width + "</rainbow_w>");
		if (rainbow.height > 0)
			sb.append("<rainbow_h>" + rainbow.height + "</rainbow_h>");
		sb.append("<minimum_temperature>" + box.view.getMinimumTemperature()
				+ "</minimum_temperature>\n");
		sb.append("<maximum_temperature>" + box.view.getMaximumTemperature()
				+ "</maximum_temperature>\n");
		if (box.view.isVelocityOn()) {
			sb.append("<velocity>true</velocity>\n");
		}
		if (box.view.isStreamlineOn()) {
			sb.append("<streamline>true</streamline>\n");
		}
		if (box.view.isGraphOn()) {
			sb.append("<graph>true</graph>\n");
		}
		if (!box.view.isClockOn()) {
			sb.append("<clock>false</clock>\n");
		}
		if (!box.view.isSmooth()) {
			sb.append("<smooth>false</smooth>\n");
		}
		int n = box.view.getTextBoxCount();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				sb.append(box.view.getTextBox(i).toXml());
			}
		}
		sb.append("</view>\n");

		sb.append("</state>\n");

		return sb.toString();

	}

}
