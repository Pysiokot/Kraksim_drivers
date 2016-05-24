package pl.edu.agh.cs.kraksim.visual.infolayer;

import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.core.Node;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoCursor;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.LaneCarInfoIface;
import pl.edu.agh.cs.kraksim.main.EvalModuleProvider;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class IntersectionPanel extends JPanel{

    Intersection crossroad;
    Map<Node, ArrayList<Link>> lanesMap;

    Vector2d center;
    static int lastPos = 0;
    final Dimension panelSize = new Dimension(800, 600);

    final int laneWidth = 18;
    final int lightRadius = 10;
    final Color background = new Color(34,139,34);
    int carToIntersectionOffset = 100;
    int oneUnitLength = 15;
    InfoProvider ip;

    JLabel statsLabel;

    final static Vector2d rotateVector = new Vector2d(1.0, 0.0);

    static BufferedImage arrowStraight = null;
    static BufferedImage arrowLeft = null;
    static BufferedImage arrowRight = null;

    public double angle(Vector2d a, Vector2d b){
        return  Math.atan2(b.y, b.x) - Math.atan2(a.y, a.x);
    }

    public void myDrawImage(Graphics2D g2d, BufferedImage image, Vector2d pos, float scale, double angle){
        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y);
        at.rotate(angle);
        at.scale(scale, scale);
        at.translate(-image.getWidth()/2, -image.getHeight()/2);
        g2d.drawImage(image, at, null);
    }

    private void loadImages() throws IOException{
        if(arrowStraight == null) arrowStraight = ImageIO.read(new File("images/straight2.png"));
        if(arrowLeft == null) arrowLeft = ImageIO.read(new File("images/left.png"));
        if(arrowRight == null) arrowRight = ImageIO.read(new File("images/right.png"));
    }

    public IntersectionPanel(Intersection inter) {
        ip = InfoProvider.getInstance();
        setPreferredSize(panelSize);
        crossroad = inter;
        center = new Vector2d(panelSize.width/2, panelSize.height/2);

        try{
            loadImages();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }

        Action a = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                rotate(rotateVector, 0.1);
                validate();
                repaint();
                System.out.println("Herp+ " + rotateVector);
            }
        };

        Action b = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                rotate(rotateVector, -0.1);
                validate();
                repaint();
                System.out.println("Herp-" + rotateVector);
            }
        };

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "W");
        this.getActionMap().put("W", a);

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "Q");
        this.getActionMap().put("Q", b);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawCenter(g, new Dimension(15 *oneUnitLength, 15 *oneUnitLength));
        drawLanes(g);
    }

    public void drawBackground(Graphics g) {
        g.setColor(background);
        g.fillRect(0, 0, (int)panelSize.getWidth(), (int)panelSize.getHeight());
    }

    public void drawCenter(Graphics g, Dimension d) {
        g.setColor(Color.DARK_GRAY);

        int pX = (int)(center.x - d.getWidth()/2);
        int pY = (int)(center.y - d.getHeight()/2);

        g.fillRect(pX, pY, d.width, d.height);
    }

    public void drawLanes(Graphics g){
        g.setColor(Color.DARK_GRAY);

        if(lanesMap == null) lanesMap = getLanesMap(crossroad.outboundLinkIterator(), crossroad.inboundLinkIterator());

        for (ArrayList<Link> lanes : lanesMap.values()) {
            drawBundleOfLanes(g, lanes);
        }

        for (ArrayList<Link> lanes : lanesMap.values()) { // inna petla zeby nie nachodzilo na siebie
            drawBundleOfCars(g, lanes);
        }
    }



    public void drawLight(Graphics g, Point2D p, Color c, int radius){
        int backgroundRadius = radius + 2;

        g.setColor(Color.BLACK);
        g.fillOval((int)(p.getX() - (backgroundRadius)*0.5),(int)(p.getY() - backgroundRadius*0.5), backgroundRadius, backgroundRadius);

        g.setColor(c);
        g.fillOval((int)(p.getX() - radius*0.5),(int)(p.getY() - radius*0.5), radius, radius);
    }

    public void drawBundleOfCars(Graphics g, ArrayList<Link> lanes){
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(laneWidth));
        int drawnLanesGlobal = 0;

        Vector2d direction = getDirection(lanes.get(0));
        Vector2d perpendicual = perpendicularVector(direction);

        direction.normalize();
        perpendicual.normalize();
        Color col = Color.YELLOW;
        CarInfoIView carInfoView = ip.getCarInfoIView();
        for(Link lane : lanes){
            CarInfoCursor cursor;
            if(isInbound(lane)&&lane.leftLaneCount()>0){
                drawCarsForLane(g2, drawnLanesGlobal, direction, perpendicual, carInfoView, lane.getLeftLane(0));
                drawnLanesGlobal++;
            }
            for(int i = 0; i < lane.mainLaneCount(); i++){
                drawCarsForLane(g2, drawnLanesGlobal, direction, perpendicual, carInfoView, lane.getMainLane(i));
                drawnLanesGlobal++;
            }

            if(isInbound(lane) && lane.rightLaneCount()>0){
                drawCarsForLane(g2, drawnLanesGlobal, direction, perpendicual, carInfoView, lane.getRightLane(0));
                drawnLanesGlobal++;
            }
        }
    }

    public int howManyCars(Lane l){
        LaneCarInfoIface carInfo = ip.getCarInfoIView().ext(l);
        CarInfoCursor infoForwardCursor = carInfo.carInfoForwardCursor();
        int count = 0;
        while (infoForwardCursor != null && infoForwardCursor.isValid()) {
            count++;
            infoForwardCursor.next();
        }
        return count;
    }

    private void drawCarsForLane(Graphics2D g2, int drawnLanesGlobal, Vector2d direction, Vector2d perpendicual, CarInfoIView carInfoView, Lane lane) {
        LaneCarInfoIface carInfo = carInfoView.ext(lane);
        CarInfoCursor infoForwardCursor = carInfo.carInfoForwardCursor();
        int offset = lane.getOffset();

        while (infoForwardCursor != null && infoForwardCursor.isValid()) {
            int position = infoForwardCursor.currentPos() + (offset);
            drawCar(g2, lane, infoForwardCursor.currentPos(), drawnLanesGlobal, direction, perpendicual, Color.yellow, isInbound(lane.getOwner()));
            infoForwardCursor.next();
        }
        drawTextForLane(g2, lane, drawnLanesGlobal, direction, perpendicual, Color.yellow);
    }

    public void drawBundleOfLanes(Graphics g, ArrayList<Link> lanes){
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(laneWidth));
        int drawnLanesGlobal = 0;

        Vector2d direction = getDirection(lanes.get(0));
        Vector2d perpendicual = perpendicularVector(direction);

        direction.normalize();
        perpendicual.normalize();
        Color col = Color.DARK_GRAY;
        CarInfoIView carInfoView = ip.getCarInfoIView();
        for(Link lane : lanes){
            CarInfoCursor cursor;
            if(isInbound(lane)&&lane.leftLaneCount()>0){
               drawLane(g2, lane.getLeftLane(0), (lane.getLeftLane(0).getLength() + 15) * oneUnitLength, drawnLanesGlobal, direction, perpendicual, col);
               drawnLanesGlobal++;
            }

            for(int i = 0; i < lane.mainLaneCount(); i++){
                drawLane(g2, lane.getMainLane(i),( lane.getMainLane(0).getLength()+ 15) * oneUnitLength, drawnLanesGlobal, direction, perpendicual, col);
                drawnLanesGlobal++;
            }

            if(isInbound(lane) && lane.rightLaneCount()>0){
                drawLane(g2, lane.getRightLane(0),(lane.getRightLane(0).getLength()+ 15) * oneUnitLength, drawnLanesGlobal, direction, perpendicual, col);
                drawnLanesGlobal++;
            }
        }
    }

    private void drawTextForLane(Graphics2D g2, Lane lane, int drawnLanes, Vector2d direction, Vector2d perpendicual, Color color ){

        Vector2d currentStart = new Vector2d(center);
        Vector2d currentPerpendicual = new Vector2d(perpendicual);
        Vector2d algorithmInfoPosition = new Vector2d(direction);

        algorithmInfoPosition.scale(55-11*drawnLanes);
        algorithmInfoPosition.add(currentStart);

        currentPerpendicual.scale((laneWidth+2)*drawnLanes);

        algorithmInfoPosition.add(currentPerpendicual);

        String toWrite = String.valueOf(InfoProvider.getInstance().getEvalIView().ext(lane).getEvaluation());
        if(toWrite.isEmpty()) toWrite = "0.0";
        drawTexts(g2,toWrite, algorithmInfoPosition, Color.ORANGE, 12);
    }

    private void drawCar(Graphics2D g2, Lane lane, int position, int drawnLanes, Vector2d direction, Vector2d perpendicual, Color color, Boolean inbound ){
        Vector2d currentDirection = new Vector2d(direction);
        Vector2d currentStart = new Vector2d(center);
        Vector2d currentPerpendicual = new Vector2d(perpendicual);


        Vector2d dirTmp = new Vector2d(currentDirection);



        int distanceToCrossroad = inbound ? lane.getLength() - position : position;
        currentDirection.scale(distanceToCrossroad * 3 * oneUnitLength + carToIntersectionOffset);
        dirTmp.scale(distanceToCrossroad * 3 * oneUnitLength + oneUnitLength + carToIntersectionOffset);
        currentDirection.add(currentStart);
        dirTmp.add(currentStart);

        currentPerpendicual.scale((laneWidth+2)*drawnLanes);
        currentDirection.add(currentPerpendicual);
        dirTmp.add(currentPerpendicual);


        g2.setColor(color);
        g2.draw(new Line2D.Double(dirTmp.x, dirTmp.y, currentDirection.x, currentDirection.y));
    }


    private Boolean isLeft(Lane l){
        return (l.getOwner().leftLaneCount() > 0 && l.getOwner().getLeftLane(0) == l);
    }

    private Boolean isRight(Lane l){
        return (l.getOwner().rightLaneCount() > 0 && l.getOwner().getRightLane(0) == l);
    }

    private void drawLane(Graphics2D g2, Lane lane,int length, int drawnLanes, Vector2d direction, Vector2d perpendicual, Color color ) {
        g2.setColor(color);

        Vector2d currentDirection = new Vector2d(direction);
        Vector2d currentPerpendicual = new Vector2d(perpendicual);
        Vector2d currentStart = new Vector2d(center);
        Vector2d lightsPosition = new Vector2d(direction);
        Vector2d arrowsPosition = new Vector2d(direction);
        Vector2d textPosition = new Vector2d(direction);

        textPosition.scale(95);
        textPosition.add(currentStart);

        lightsPosition.scale(75);
        lightsPosition.add(currentStart);

        arrowsPosition.scale(120);
        arrowsPosition.add(currentStart);

        currentDirection.scale(length);
        currentDirection.add(currentStart);

        currentPerpendicual.scale((laneWidth+2)*drawnLanes);


        textPosition.add(currentPerpendicual);
        lightsPosition.add(currentPerpendicual);
        arrowsPosition.add(currentPerpendicual);
        currentDirection.add(currentPerpendicual);
        currentStart.add(currentPerpendicual);

        g2.draw(new Line2D.Double(currentStart.x, currentStart.y, currentDirection.x, currentDirection.y));
        drawTexts(g2,String.valueOf(howManyCars(lane)), textPosition, Color.WHITE, 15);
        drawArrows(g2, lane, direction, lightsPosition, arrowsPosition);
       }

    private void drawArrows(Graphics2D g2, Lane lane, Vector2d direction, Vector2d lightsPosition, Vector2d arrowsPosition) {
        if(isInbound(lane.getOwner())){
            Color c = ip.getBlockView().ext(lane).isBlocked() ? Color.RED : Color.GREEN;
            drawLight(g2, new Point((int)lightsPosition.x, (int)lightsPosition.y), c, lightRadius );

            lightsPosition.scale(0.9);
            if(isLeft(lane)){
                myDrawImage(g2, arrowLeft, arrowsPosition, 0.05f, Math.PI - 1.0 * (angle(direction, rotateVector)));
            }
            else if(isRight(lane)){
                myDrawImage(g2, arrowRight, arrowsPosition, 0.05f, Math.PI - 1.0 * (angle(direction, rotateVector)));
            }
            else{
                myDrawImage(g2, arrowStraight, arrowsPosition, 0.05f, Math.PI - 1.0 * (angle(direction, rotateVector)));
                System.out.println(direction + "\t" +  Math.toDegrees(direction.angle(rotateVector)));
            }
        }
    }

    private void drawTexts(Graphics2D g2, String text, Vector2d textPosition, Color c, int size) {
        Font f = new Font("TimesRoman", Font.BOLD, size);
        g2.setColor(c);
        g2.setFont(f);

        FontMetrics fm   = g2.getFontMetrics(f);
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(text, g2);
        int textHeight = (int)(rect.getHeight());
        int textWidth  = (int)(rect.getWidth());

        g2.drawString(text, (float)textPosition.x-(textWidth/2), (float)textPosition.y-(textHeight/2)+ fm.getAscent());
    }

    public void rotate(Vector2d v, double n)
    {
        double rx = (v.x * Math.cos(n)) - (v.y * Math.sin(n));
        double ry = (v.x * Math.sin(n)) + (v.y * Math.cos(n));
        v.x = rx;
        v.y = ry;
    }

    private void populateMap(Map<Node, ArrayList<Link>> map, Link l, Node n){
        ArrayList<Link> values = map.get(n);
        if (values == null) {
            values = new ArrayList<>();
            map.put(n, values);
        }
        values.add(l);
    }

    private Map<Node, ArrayList<Link>> getLanesMap(Iterator<Link> outgoing, Iterator<Link> ingoing){
        Map<Node, ArrayList<Link>> map = new HashMap<>();
        while (outgoing.hasNext()) {
            Link out = outgoing.next();
            populateMap(map, out, out.getEnd());
        }
        while (ingoing.hasNext()) {
            Link in = ingoing.next();
            populateMap(map, in, in.getBeginning());
        }
        return  map;
    }

    public Vector2d getDirection(Link lane){
        double dx = lane.getEnd().getPoint().getX() - lane.getBeginning().getPoint().getX();
        double dy = lane.getEnd().getPoint().getY() -lane.getBeginning().getPoint().getY();
        return new Vector2d(dx,dy);
    }

    public Boolean isInbound(Link l){
        return l.getEnd().equals(crossroad);
    }
    public void setLabel(JLabel l){
        statsLabel = l;
    }

    public Vector2d perpendicularVector(Vector2d v){
        return new Vector2d(v.y, - v.x);
    }

    public void update() {
        EvalModuleProvider evalProvider = InfoProvider.getInstance().getEvalProvider();
        if(statsLabel != null) statsLabel.setText(evalProvider.toString());
        validate();
        repaint();
    }
}
