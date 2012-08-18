/*
 *   Copyright (C) 2009  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RectangularShape;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.math.Polygon2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.util.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class PartModelDialog extends JDialog {

	private final static DecimalFormat FORMAT = new DecimalFormat("####.######");

	private JTextField thermalConductivityField;
	private JTextField specificHeatField;
	private JTextField densityField;
	private JLabel powerLabel;
	private JTextField powerField;
	private JLabel temperatureLabel;
	private JTextField temperatureField;
	private JTextField windSpeedField;
	private JTextField windAngleField;
	private JTextField absorptionField;
	private JTextField reflectionField;
	private JTextField transmissionField;
	private JTextField emissivityField;
	private JTextField xField, yField, wField, hField, angleField, scaleField;
	private JTextField uidField;
	private JTextField labelField;
	private JRadioButton notHeatSourceRadioButton;
	private JRadioButton powerRadioButton;
	private JRadioButton constantTemperatureRadioButton;
	private Window owner;
	private ActionListener okListener;

	PartModelDialog(final View2D view, final Part part, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Part (#" + view.model.getParts().indexOf(part) + ") Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.repaint();
				dispose();
			}
		});

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float absorption = parse(absorptionField.getText());
				if (Float.isNaN(absorption))
					return;
				float reflection = parse(reflectionField.getText());
				if (Float.isNaN(reflection))
					return;
				float transmission = parse(transmissionField.getText());
				if (Float.isNaN(transmission))
					return;
				float emissivity = parse(emissivityField.getText());
				if (Float.isNaN(emissivity))
					return;

				if (absorption < 0 || absorption > 1) {
					JOptionPane.showMessageDialog(owner, "Absorption coefficient must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (reflection < 0 || reflection > 1) {
					JOptionPane.showMessageDialog(owner, "Reflection coefficient must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (transmission < 0 || transmission > 1) {
					JOptionPane.showMessageDialog(owner, "Transmission coefficient must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (emissivity < 0 || emissivity > 1) {
					JOptionPane.showMessageDialog(owner, "Emissivity must be within [0, 1].", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				float sum = absorption + reflection + transmission;
				if (Math.abs(sum - 1) > 0.01) {
					JOptionPane.showMessageDialog(owner, "The sum of absorption, reflection, and transmission must be exactly one.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				float conductivity = parse(thermalConductivityField.getText());
				if (Float.isNaN(conductivity))
					return;
				float capacity = parse(specificHeatField.getText());
				if (Float.isNaN(capacity))
					return;
				float density = parse(densityField.getText());
				if (Float.isNaN(density))
					return;
				float windSpeed = parse(windSpeedField.getText());
				if (Float.isNaN(windSpeed))
					return;
				float windAngle = parse(windAngleField.getText());
				if (Float.isNaN(windAngle))
					return;
				float xcenter = parse(xField.getText());
				if (Float.isNaN(xcenter))
					return;
				float ycenter = parse(yField.getText());
				if (Float.isNaN(ycenter))
					return;
				float width = Float.NaN;
				if (wField != null) {
					width = parse(wField.getText());
					if (Float.isNaN(width))
						return;
				}
				float height = Float.NaN;
				if (hField != null) {
					height = parse(hField.getText());
					if (Float.isNaN(height))
						return;
				}
				float degree = Float.NaN;
				if (angleField != null) {
					degree = parse(angleField.getText());
					if (Float.isNaN(degree))
						return;
				}
				float scale = Float.NaN;
				if (scaleField != null) {
					scale = parse(scaleField.getText());
					if (Float.isNaN(scale))
						return;
					if (scale <= 0) {
						JOptionPane.showMessageDialog(owner, "Scale must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				String uid = uidField.getText();
				if (uid != null) {
					uid = uid.trim();
					if (!uid.equals("") && !uid.equals(part.getUid())) {
						if (view.model.isUidUsed(uid)) {
							JOptionPane.showMessageDialog(owner, "UID: " + uid + " has been taken.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				if (notHeatSourceRadioButton.isSelected() || constantTemperatureRadioButton.isSelected()) {
					float temperature = parse(temperatureField.getText());
					if (Float.isNaN(temperature))
						return;
					part.setTemperature(temperature);
					part.setPower(0);
				} else if (powerRadioButton.isSelected()) {
					float power = parse(powerField.getText());
					if (Float.isNaN(power))
						return;
					part.setPower(power);
				}
				part.setConstantTemperature(constantTemperatureRadioButton.isSelected());

				Shape shape = part.getShape();
				if (shape instanceof RectangularShape) {
					if (!Float.isNaN(width) && !Float.isNaN(height)) {
						view.resizeManipulableTo(part, xcenter - 0.5f * width, view.model.getLy() - ycenter - 0.5f * height, width, height, 0, 0);
					}
				} else if (shape instanceof Polygon2D) {
					if (!Float.isNaN(degree) && degree != 0) {
						((Polygon2D) part.getShape()).rotateBy(degree);
					}
					if (!Float.isNaN(scale) && scale != 1) {
						((Polygon2D) part.getShape()).scale(scale);
					}
				}

				part.setWindAngle((float) Math.toRadians(windAngle));
				part.setWindSpeed(windSpeed);
				part.setThermalConductivity(Math.max(conductivity, 0.000000001f));
				part.setSpecificHeat(capacity);
				part.setDensity(density);
				part.setAbsorption(absorption);
				part.setReflection(reflection);
				part.setTransmission(transmission);
				part.setEmissivity(emissivity);
				part.setLabel(labelField.getText());
				part.setUid(uid);

				view.notifyManipulationListeners(part, ManipulationEvent.PROPERTY_CHANGE);
				view.setSelectedManipulable(view.getSelectedManipulable());
				view.repaint();

				dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(button);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(tabbedPane, BorderLayout.CENTER);

		JPanel p = new JPanel(new SpringLayout());
		JPanel pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Geometrical");
		int count = 0;

		p.add(new JLabel("Center x"));
		xField = new JTextField(FORMAT.format(part.getCenter().x));
		xField.addActionListener(okListener);
		p.add(xField);
		p.add(new JLabel("<html><i>m</i></html>"));

		p.add(new JLabel("Center y"));
		yField = new JTextField(FORMAT.format(view.model.getLy() - part.getCenter().y));
		yField.addActionListener(okListener);
		p.add(yField);
		p.add(new JLabel("<html><i>m</i></html>"));
		count++;

		if (part.getShape() instanceof RectangularShape) {

			p.add(new JLabel("Width"));
			wField = new JTextField(FORMAT.format(part.getShape().getBounds2D().getWidth()));
			wField.addActionListener(okListener);
			p.add(wField);
			p.add(new JLabel("<html><i>m</i></html>"));

			p.add(new JLabel("Height"));
			hField = new JTextField(FORMAT.format(part.getShape().getBounds2D().getHeight()));
			hField.addActionListener(okListener);
			p.add(hField);
			p.add(new JLabel("<html><i>m</i></html>"));
			count++;

		} else if (part.getShape() instanceof Polygon2D) {

			p.add(new JLabel("Rotate"));
			angleField = new JTextField("0");
			angleField.addActionListener(okListener);
			p.add(angleField);
			p.add(new JLabel("<html>&deg;</html>"));

			p.add(new JLabel("Scale"));
			scaleField = new JTextField("1");
			scaleField.addActionListener(okListener);
			p.add(scaleField);
			p.add(new JLabel("Must be a positive number."));
			count++;

		}

		MiscUtil.makeCompactGrid(p, count, 6, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Source");
		count = 0;

		ButtonGroup bg = new ButtonGroup();
		notHeatSourceRadioButton = new JRadioButton("Not a heat source");
		notHeatSourceRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
				}
			}
		});
		p.add(notHeatSourceRadioButton);
		bg.add(notHeatSourceRadioButton);

		constantTemperatureRadioButton = new JRadioButton("Constant temperature");
		constantTemperatureRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(true);
					temperatureField.setEnabled(true);
					powerLabel.setEnabled(false);
					powerField.setEnabled(false);
				}
			}
		});
		p.add(constantTemperatureRadioButton);
		bg.add(constantTemperatureRadioButton);

		powerRadioButton = new JRadioButton("Constant power");
		powerRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					temperatureLabel.setEnabled(false);
					temperatureField.setEnabled(false);
					powerLabel.setEnabled(true);
					powerField.setEnabled(true);
				}
			}
		});
		p.add(powerRadioButton);
		bg.add(powerRadioButton);
		count++;

		powerLabel = new JLabel("Power density");
		p.add(powerLabel);
		powerField = new JTextField(FORMAT.format(part.getPower()), 16);
		powerField.addActionListener(okListener);
		p.add(powerField);
		p.add(new JLabel("<html><i>W/m<sup><font size=2>3</font></sup></html>"));
		count++;

		temperatureLabel = new JLabel("Temperature");
		p.add(temperatureLabel);
		temperatureField = new JTextField(FORMAT.format(part.getTemperature()), 16);
		temperatureField.addActionListener(okListener);
		p.add(temperatureField);
		p.add(new JLabel("<html><i>\u2103</i></html>"));
		count++;

		p.add(new JLabel("Wind speed"));
		windSpeedField = new JTextField(FORMAT.format(part.getWindSpeed()), 8);
		windSpeedField.addActionListener(okListener);
		p.add(windSpeedField);
		p.add(new JLabel("<html><i>m/s</i></html>"));
		count++;

		p.add(new JLabel("Wind angle"));
		windAngleField = new JTextField(FORMAT.format(Math.toDegrees(part.getWindAngle())), 8);
		windAngleField.addActionListener(okListener);
		p.add(windAngleField);
		p.add(new JLabel("Degrees"));
		count++;

		if (part.getPower() != 0) {
			powerRadioButton.setSelected(true);
		} else if (part.getConstantTemperature()) {
			constantTemperatureRadioButton.setSelected(true);
		} else {
			notHeatSourceRadioButton.setSelected(true);
		}

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Thermal");
		count = 0;

		p.add(new JLabel("Thermal conductivity"));
		thermalConductivityField = new JTextField(FORMAT.format(part.getThermalConductivity()), 8);
		thermalConductivityField.addActionListener(okListener);
		p.add(thermalConductivityField);
		p.add(new JLabel("<html><i>W/(m\u00b7\u2103)</i></html>"));
		count++;

		p.add(new JLabel("Specific heat"));
		specificHeatField = new JTextField(FORMAT.format(part.getSpecificHeat()), 8);
		specificHeatField.addActionListener(okListener);
		p.add(specificHeatField);
		p.add(new JLabel("<html><i>J/(kg\u00b7\u2103)</i></html>"));
		count++;

		p.add(new JLabel("Density"));
		densityField = new JTextField(FORMAT.format(part.getDensity()), 8);
		densityField.addActionListener(okListener);
		p.add(densityField);
		p.add(new JLabel("<html><i>kg/m<sup><font size=2>3</font></sup></html>"));
		count++;

		MiscUtil.makeCompactGrid(p, count, 3, 5, 5, 10, 2);

		p = new JPanel(new SpringLayout());
		pp = new JPanel(new BorderLayout());
		pp.add(p, BorderLayout.NORTH);
		tabbedPane.add(pp, "Optical");
		count = 0;

		p.add(new JLabel("Absorption"));
		absorptionField = new JTextField(FORMAT.format(part.getAbsorption()), 8);
		absorptionField.addActionListener(okListener);
		p.add(absorptionField);

		p.add(new JLabel("Reflection"));
		reflectionField = new JTextField(FORMAT.format(part.getReflection()), 8);
		reflectionField.addActionListener(okListener);
		p.add(reflectionField);
		count++;

		p.add(new JLabel("Transmission"));
		transmissionField = new JTextField(FORMAT.format(part.getTransmission()), 16);
		transmissionField.addActionListener(okListener);
		p.add(transmissionField);

		p.add(new JLabel("Emissivity"));
		emissivityField = new JTextField(FORMAT.format(part.getEmissivity()), 16);
		emissivityField.addActionListener(okListener);
		p.add(emissivityField);
		count++;

		MiscUtil.makeCompactGrid(p, count, 4, 5, 5, 10, 2);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pp.add(p, BorderLayout.CENTER);
		p.add(new JLabel("<html><br><hr align=left width=100>1) All the above coefficients must be within [0, 1].<br>2) The sum of the absorption, reflection, and transmission coefficients must be exactly one.</html>"));

		Box miscBox = Box.createVerticalBox();
		pp = new JPanel(new BorderLayout());
		pp.add(miscBox, BorderLayout.NORTH);
		tabbedPane.add(pp, "Miscellaneous");

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		p.add(new JLabel("Unique ID:"));
		uidField = new JTextField(part.getUid(), 20);
		uidField.addActionListener(okListener);
		p.add(uidField);
		p.add(new JLabel("Label:"));
		labelField = new JTextField(part.getLabel(), 20);
		labelField.addActionListener(okListener);
		p.add(labelField);

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		miscBox.add(p);
		p.add(new JLabel("<html><br><hr align=left width=100>1) Set a unique ID if you need to find this part in scripts.<br>2) The label will be shown on top of this part in the view.</html>"));

		pack();
		setLocationRelativeTo(view);

	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse: " + s, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
