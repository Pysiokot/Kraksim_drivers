package pl.edu.agh.cs.kraksimtrafficgenerator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;

public class TrafficGeneratorGUI {
    private TrafficGenerator generator;
    private static TrafficGeneratorGUI window;
    private String pathToFile = "input\\maps\\krakow_duzy\\trafficSchemes\\";

    private JFrame frame;
    private JTextField tfCarCount;
    private JTextField tfStart;
    private JTextField tfEnd;
    private JTextField tfFileName;
    private JComboBox<String> comboBoxSchedule;
    private JComboBox<Character> comboBoxGatewaySymbol;
    private JButton btnGenerate;
    private JLabel lblStart;
    private JLabel lblEnd;
    private JSpinner spinnerNumberOfGateways;

    private GenerateCallback callback;

    public interface GenerateCallback {
        void call();
    }

    public String getPathToFile() {
        return pathToFile + tfFileName.getText() + ".xml";
    }

    public void setPathToFile(String pathToFile) {
        File file = new File(pathToFile);
        if(!file.exists()) {
            file.mkdir();
        }
        this.pathToFile = pathToFile;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    window = new TrafficGeneratorGUI();
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    /**
     * Create the application.
     */
    public TrafficGeneratorGUI() {
        this(null);
    }

    public TrafficGeneratorGUI(GenerateCallback callback) {
        this.callback = callback;
        initialize();
        addListeners();
        fillValues();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("KraksimTrafficGenerator");

        JLabel lblCarCount = new JLabel("Car count:");
        lblCarCount.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblCarCount.setBounds(10, 47, 78, 14);
        frame.getContentPane().add(lblCarCount);

        tfCarCount = new JTextField();
        tfCarCount.setBounds(88, 46, 86, 20);
        frame.getContentPane().add(tfCarCount);
        tfCarCount.setColumns(10);

        JLabel lblSchedule = new JLabel("Schedule:");
        lblSchedule.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblSchedule.setBounds(10, 87, 70, 14);
        frame.getContentPane().add(lblSchedule);

        comboBoxSchedule = new JComboBox<>();
        comboBoxSchedule.setBounds(86, 86, 76, 20);
        frame.getContentPane().add(comboBoxSchedule);

        tfStart = new JTextField();
        tfStart.setBounds(185, 86, 86, 20);
        frame.getContentPane().add(tfStart);
        tfStart.setColumns(10);

        tfEnd = new JTextField();
        tfEnd.setBounds(299, 86, 86, 20);
        frame.getContentPane().add(tfEnd);
        tfEnd.setColumns(10);

        lblStart = new JLabel("Start");
        lblStart.setBounds(214, 73, 46, 14);
        frame.getContentPane().add(lblStart);

        lblEnd = new JLabel("End");
        lblEnd.setBounds(330, 73, 46, 14);
        frame.getContentPane().add(lblEnd);

        JLabel lblGatewaySymbol = new JLabel("Gateway symbol:");
        lblGatewaySymbol.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblGatewaySymbol.setBounds(10, 128, 117, 19);
        frame.getContentPane().add(lblGatewaySymbol);

        comboBoxGatewaySymbol = new JComboBox<>();
        comboBoxGatewaySymbol.setBounds(137, 129, 37, 20);
        frame.getContentPane().add(comboBoxGatewaySymbol);

        JLabel lblNumberOfGateways = new JLabel("Number of Gateways:");
        lblNumberOfGateways.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblNumberOfGateways.setBounds(10, 170, 152, 19);
        frame.getContentPane().add(lblNumberOfGateways);

        spinnerNumberOfGateways = new JSpinner();
        spinnerNumberOfGateways.setBounds(161, 171, 40, 20);
        frame.getContentPane().add(spinnerNumberOfGateways);

        btnGenerate = new JButton("Generate!");
        btnGenerate.setBounds(161, 212, 111, 39);
        frame.getContentPane().add(btnGenerate);

        JLabel lblFileName = new JLabel("File name:");
        lblFileName.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblFileName.setBounds(10, 11, 78, 14);
        frame.getContentPane().add(lblFileName);

        tfFileName = new JTextField();
        tfFileName.setBounds(88, 10, 86, 20);
        frame.getContentPane().add(tfFileName);
        tfFileName.setColumns(10);
    }

    private void addListeners() {

        comboBoxSchedule.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    String item = (String) event.getItem();
                    switch (item) {
                        case "normal":
                            lblStart.setText("Time");
                            lblEnd.setText("Dev");
                            tfEnd.setEditable(true);
                            break;
                        case "point":
                            lblStart.setText("Time");
                            tfEnd.setEditable(false);
                            break;
                        default:
                            lblStart.setText("Start");
                            lblEnd.setText("End");
                            tfEnd.setEditable(true);
                    }
                }
            }
        });

        btnGenerate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (tfFileName.getText().equals(""))
                    JOptionPane.showMessageDialog(frame, "Please specify file name for new traffic.");
                else {
                    generator = new TrafficGenerator.GeneratorBuilder(tfFileName.getText(), tfCarCount.getText())
                            .schedule(comboBoxSchedule.getSelectedItem().toString())
                            .start(tfStart.getText())
                            .end(tfEnd.getText())
                            .gatewaySymbol(comboBoxGatewaySymbol.getSelectedItem().toString())
                            .gatewaysCount((int) spinnerNumberOfGateways.getValue())
                            .pathToFile(pathToFile)
                            .build();
                    generator.generateFile();
                    frame.dispose();
                    if (callback != null) {
                        callback.call();
                    }
                }
            }
        });

    }

    private void fillValues() {
        comboBoxSchedule.addItem("uniform");
        comboBoxSchedule.addItem("normal");
        comboBoxSchedule.addItem("point");

        for (char letter = 'A'; letter <= 'Z'; letter++) {
            comboBoxGatewaySymbol.addItem(letter);
        }

        comboBoxSchedule.setSelectedItem("uniform");
        comboBoxGatewaySymbol.setSelectedItem('G');
        tfStart.setText("0");
        tfEnd.setText("8000");
        tfCarCount.setText("100");
        spinnerNumberOfGateways.setValue(14);
    }

    public void readValuesFromFile(String filePath) {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(new File(filePath));
            Element root = doc.getRootElement();

            int schemeCount = root.getChildren().size();

            Element firstScheme = (Element) root.getChildren().get(0);

            String carCount = firstScheme.getAttributeValue("count");

            int gatewayCount = (int)(Math.sqrt(1 + 4*schemeCount) - 1) / 2;

            Element gateway = firstScheme.getChild("gateway");
            String gatewayId = gateway.getAttributeValue("id");

            String gatewaySymbol = gatewayId.substring(0, 1);

            Element scheduleElement = (Element) gateway.getChildren().get(0);

            String scheduleName = scheduleElement.getName();

            String start;
            String end;

            switch(scheduleName) {
                case "normal":
                    start = scheduleElement.getAttributeValue("y");
                    end = scheduleElement.getAttributeValue("dev");
                    break;

                case "point":
                    start = scheduleElement.getAttributeValue("y");
                    end = "8000";
                    break;

                default:
                    start = scheduleElement.getAttributeValue("a");
                    end = scheduleElement.getAttributeValue("b");

            }

            comboBoxSchedule.setSelectedItem(scheduleName);
            comboBoxGatewaySymbol.setSelectedItem(gatewaySymbol);
            tfStart.setText(start);
            tfEnd.setText(end);
            tfCarCount.setText(carCount);
            spinnerNumberOfGateways.setValue(gatewayCount);

        } catch (Exception ignored) {

        }
    }

}
