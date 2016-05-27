package pl.edu.agh.cs.kraksimtrafficgenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TrafficGenerator {
    private List<String> lines;
    private String pathToFile;

    private final String fileName;
    private final String carCount;
    private String schedule;
    private String start;
    private String end;
    private String gatewaySymbol;
    private int gatewaysCount;

    public TrafficGenerator(GeneratorBuilder builder) {
        this.fileName = builder.fileName;
        this.carCount = builder.carCount;
        this.schedule = builder.schedule;
        this.start = builder.start;
        this.end = builder.end;
        this.gatewaySymbol = builder.gatewaySymbol;
        this.gatewaysCount = builder.gatewaysCount;
        this.pathToFile = builder.pathToFile;
    }


    public static class GeneratorBuilder {
        private final String fileName;
        private final String carCount;
        private String schedule;
        private String start;
        private String end;
        private String gatewaySymbol;
        private int gatewaysCount;
        private String pathToFile;

        public GeneratorBuilder(String fileName, String carCount) {
            this.fileName = fileName;
            this.carCount = carCount;
        }

        public GeneratorBuilder schedule(String schedule) {
            this.schedule = schedule;
            return this;
        }

        public GeneratorBuilder start(String start) {
            this.start = start;
            return this;
        }

        public GeneratorBuilder end(String end) {
            this.end = end;
            return this;
        }

        public GeneratorBuilder gatewaySymbol(String gatewaySymbol) {
            this.gatewaySymbol = gatewaySymbol;
            return this;
        }

        public GeneratorBuilder gatewaysCount(int gatewaysCount) {
            this.gatewaysCount = gatewaysCount;
            return this;
        }

        public GeneratorBuilder pathToFile(String pathToFile) {
            this.pathToFile = pathToFile;
            return this;
        }

        public TrafficGenerator build() {
            return new TrafficGenerator(this);
        }
    }


    public void generateFile() {
        generateValues();
        writeToFile(fileName);
    }

    private void generateValues() {
        lines = new ArrayList<>();
        String chosenSchedule;

        switch (schedule) {
            case "normal":
                chosenSchedule = "\t\t\t<normal y='" + start + "' dev='" + end + "' />";
                break;
            case "point":
                chosenSchedule = "\t\t\t<point y='" + start + "' />";
                break;
            default:
                chosenSchedule = "\t\t\t<uniform a='" + start + "' b='" + end + "' />";
        }

        lines.add("<?xml version=\"1.0\"?>");
        lines.add("<traffic>");
        for (int i = 0; i < gatewaysCount; i++) {
            for (int j = 0; j < gatewaysCount; j++) {
                if (i == j) continue;
                lines.add("\t<scheme count='" + carCount + "'>");
                lines.add("\t\t<gateway id='" + gatewaySymbol + i + "'>");
                lines.add(chosenSchedule);
                lines.add("\t\t</gateway>");
                lines.add("\t\t<gateway id='" + gatewaySymbol + j + "' />");
                lines.add("\t</scheme>");
            }
        }
        lines.add("</traffic>");

    }

    private void writeToFile(String fileName) {
        Path file = Paths.get(pathToFile + fileName + ".xml");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
