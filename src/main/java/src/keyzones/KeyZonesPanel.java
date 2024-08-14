package src.keyzones;

import kotlin.Pair;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class KeyZonesPanel extends JPanel {
    private JTabbedPane chartTabbedPane = new JTabbedPane();
    private static final Logger logger = LoggerFactory.getLogger(KeyZonesPanel.class);

    public KeyZonesPanel() {
        init();
    }

    private void init() {
        logger.info("Initializing key zones panel");
        this.setLayout(new BorderLayout());
        add(chartTabbedPane, BorderLayout.CENTER);
        initGridPanel();
    }

    public void refreshChart(String startDate, String endDate) {
        logger.info("Updating key zones panel with start date: {}, end date: {}", startDate, endDate);
        chartTabbedPane.removeAll();
        for (Coin coin : Coin.values()) {
            JTabbedPane upperTabbedPane = new JTabbedPane();

            for (TimeFrame tf : TimeFrame.values()) {
                KeyZonesRunner runner = new KeyZonesRunner(coin, tf, startDate, endDate);
                Pair<ArrayList<String>, String> output = runner.run();
                ArrayList<String> zones = output.getFirst(); // TODO
                upperTabbedPane.add("Timeframe " + tf.getTimeFrame(), getChartPanel(output.getSecond()));
            }
            chartTabbedPane.add(coin.toString(), upperTabbedPane);
        }
        chartTabbedPane.revalidate();
        chartTabbedPane.repaint();
    }

    private JPanel getChartPanel(String imagePath) {
        JPanel panel = new JPanel();
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            JLabel label = new JLabel(new ImageIcon(image));
            panel.add(label);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return panel;
    }

    private void initGridPanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        Font newButtonFont = new Font("Arial", Font.BOLD, 22);

        UtilDateModel firstModel = new UtilDateModel();
        Properties fp = new Properties();
        fp.put("text.today", "Today");
        fp.put("text.month", "Month");
        fp.put("text.year", "Year");
        JDatePickerImpl firstDatePicker = new JDatePickerImpl(new JDatePanelImpl(firstModel, fp), new DateLabelFormatter());

        UtilDateModel secondModel = new UtilDateModel();
        Properties sp = new Properties();
        sp.put("text.today", "Today");
        sp.put("text.month", "Month");
        sp.put("text.year", "Year");
        JDatePickerImpl secondDatePicker = new JDatePickerImpl(new JDatePanelImpl(secondModel, sp), new DateLabelFormatter());

        firstDatePicker.getJFormattedTextField().setText("Start Date");
        secondDatePicker.getJFormattedTextField().setText("End Date");

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(newButtonFont);
        refreshButton.setBackground(Color.LIGHT_GRAY);
        refreshButton.addActionListener(e -> {
            new Thread(() -> {
                // Get the selected date
                Date firstDate = (Date) firstDatePicker.getModel().getValue();
                Date secondDate = (Date) secondDatePicker.getModel().getValue();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedFirstDate = firstDate != null ? dateFormat.format(firstDate) : "None";
                String formattedSecondDate = secondDate!= null? dateFormat.format(secondDate) : "None";
                refreshChart(formattedFirstDate, formattedSecondDate);
            }).start();
        });

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        gridPanel.add(refreshButton);
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gridPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        gridPanel.add(firstDatePicker);
        gridPanel.add(Box.createRigidArea(new Dimension(15, 15)));
        gridPanel.add(secondDatePicker);
        gridPanel.add(Box.createRigidArea(new Dimension(15, 15)));

        sidePanel.add(gridPanel);
        sidePanel.setBackground(Color.LIGHT_GRAY);

        add(sidePanel, BorderLayout.EAST);
    }

    private class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    public void clearTempDirectory() {
        KeyZonesRunner.deleteTempDirectory();
    }
}
