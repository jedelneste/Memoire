package org.vadere.gui.topographycreator.view;

import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.topographycreator.control.*;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.util.geometry.GrahamScan;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoableEditSupport;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActionPedestrianDensityZoneDialog {

    public enum TARGET_OPTION_ZONE { RANDOM, NEAREST }

    enum DrawingState { START, ADD}

    private IDrawPanelModel panelModel;

    private JTextField firstPedIdField;
    private JTextField pedestrianDensityField;
    private JTextField boundaryRectangleField;
    private JTextField seedField;
    private JTextField targetsField;

    private JRadioButton rbTargetRandom;
    private JRadioButton rbTargetNearest;

    private JButton drawZoneButton;

    private JPanel panelWindow;
    private JPanel panelRadioButtons;

    private boolean valid;
    private int firstPedId;
    private double pedestrianDensity;
    private Rectangle2D.Double boundaryRectangle;
    private Random random;
    private int seed;


    public ActionPedestrianDensityZoneDialog(IDrawPanelModel<?> panelModel) {

        this.panelModel = panelModel;
        valid = true;
        firstPedId = 1;
        boundaryRectangle = new Rectangle2D.Double();
        pedestrianDensity = 1.0;
        seed = -1;

        createGuiElements();
        groupGuiElements();
        placeGuiElements();

    }

    private void createGuiElements(){
        firstPedIdField = new JTextField(String.format(Locale.US, "%d", firstPedId), 15);
        firstPedIdField.setHorizontalAlignment(JTextField.RIGHT);
        firstPedIdField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void handle(DocumentEvent e) {
                String text = firstPedIdField.getText();
                try {
                    firstPedId = Integer.parseInt(text);
                    setValid(true, firstPedIdField);
                } catch (Exception ex){
                    setValid(false, firstPedIdField);
                }
            }
        });

        DecimalFormat numberFormat = new DecimalFormat("#.00");
        pedestrianDensityField = new JTextField(numberFormat.format(pedestrianDensity), 15);
        pedestrianDensityField.setHorizontalAlignment(JTextField.RIGHT);
        pedestrianDensityField.getDocument().addDocumentListener(new SimpleDocumentListener() {

            @Override
            public void handle(DocumentEvent e) {
                String text = pedestrianDensityField.getText();
                try {
                    pedestrianDensity = Double.parseDouble(text);
                    setValid(true, pedestrianDensityField);
                } catch (Exception ex){
                    setValid(false, pedestrianDensityField);
                }
            }
        });

        createTargetRadioButtons();

        seedField = new JTextField(String.format(Locale.US, "%d", seed), 15);
        seedField.setHorizontalAlignment(JTextField.RIGHT);
        seedField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void handle(DocumentEvent e) {
                try{
                    seed = Integer.parseInt(seedField.getText());
                    setValid(true, seedField);
                } catch (Exception ex){
                    setValid(false, seedField);
                }
            }
        });


        panelWindow = new JPanel();
        panelRadioButtons = new JPanel();

    }


    private void groupGuiElements(){

        panelRadioButtons.setLayout(new FlowLayout());

        panelRadioButtons.add(rbTargetRandom);
        panelRadioButtons.add(rbTargetNearest);

    }

    private void placeGuiElements(){
        panelWindow.setLayout(new GridBagLayout());
        int row = 0;
        int col0 = 0;
        int col1 = 1;

        panelWindow.add(new JLabel("First Pedestrian Id"), c(GridBagConstraints.HORIZONTAL, col0, row));
        panelWindow.add(firstPedIdField, c(GridBagConstraints.HORIZONTAL, col1, row++));

        panelWindow.add(new JLabel("Set Pedestrian Density of the zone"), c(GridBagConstraints.HORIZONTAL, col0, row));
        panelWindow.add(pedestrianDensityField, c(GridBagConstraints.HORIZONTAL, col1, row++));

        panelWindow.add(new JLabel("Set Targets"), c(GridBagConstraints.HORIZONTAL, col0, row));
        panelWindow.add(panelRadioButtons, c(GridBagConstraints.HORIZONTAL, col1, row++));
        //panelWindow.add(targetsField, c(GridBagConstraints.HORIZONTAL, col1, row++));

        panelWindow.add(new JLabel("Set Random Seed (-1 for random)"), c(GridBagConstraints.HORIZONTAL, col0, row));
        panelWindow.add(seedField, c(GridBagConstraints.HORIZONTAL, col1, row++));

    }


    private void createTargetRadioButtons() {

        rbTargetRandom = new JRadioButton(Messages.getString("TopographyCreator.PlaceRandomPedestrians.targetRandomOption.label"), false);
        rbTargetNearest = new JRadioButton(Messages.getString("TopographyCreator.PlaceRandomPedestrians.targetNearestOption.label"), false);

        //targetsField.setEditable(false);

        ButtonGroup buttonGroupTarget = new ButtonGroup();
        buttonGroupTarget.add(rbTargetRandom);
        buttonGroupTarget.add(rbTargetNearest);

    }

    // Setter
    private void setValid(boolean valid, JComponent causingElement) {
        this.valid = valid;

        Color color = (valid) ? Color.BLACK : Color.RED;
        causingElement.setForeground(color);
    }

    private GridBagConstraints c(int fill, int gridx, int gridy){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = fill;
        c.gridx = gridx;
        c.gridy = gridy;
        c.insets = new Insets(2,2,2,2);

        return c;
    }

    public double getPedestrianDensity() {
        return pedestrianDensity;
    }

    public Random getRandom(){
        if (random == null){
            if (seed == -1){
                random = new Random();
            } else {
                random = new Random(seed);
            }
        }
        return random;
    }

    public boolean isValid() {
        return valid;
    }

    public int getFirstPedId(){
        return firstPedId;
    }

    public ActionPedestrianDensityZoneDialog.TARGET_OPTION_ZONE getTargetOption() {
        TARGET_OPTION_ZONE selectedOption;

        if (rbTargetRandom.isSelected()) {
            selectedOption = TARGET_OPTION_ZONE.RANDOM;
        } else if (rbTargetNearest.isSelected()) {
            selectedOption = TARGET_OPTION_ZONE.NEAREST;
        } else {
            throw new IllegalArgumentException("No valid target option selected!");
        }

        return selectedOption;
    }


    // Methods
    public boolean showDialog() {
        int returnValue = JOptionPane.showConfirmDialog(
                ProjectView.getMainWindow(),
                panelWindow,
                Messages.getString("TopographyCreator.PlaceRandomPedestrians.label"),
                JOptionPane.OK_CANCEL_OPTION);

        return returnValue == JOptionPane.OK_OPTION;
    }
}
