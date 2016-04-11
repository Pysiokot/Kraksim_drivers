package pl.edu.agh.cs.kraksim.visual.infolayer.messages;

import java.awt.Point;

import pl.edu.agh.cs.kraksim.core.Intersection;

public class NodeMessageWindowThread extends Thread {
	private Intersection intersection;
	private Point point;
    private MessageWindow messageWindow;

    public NodeMessageWindowThread(Intersection intersection, Point p){
		this.intersection = intersection;
		this.point = p;
	}
	
	public void update(){
        if (messageWindow != null){
            messageWindow.update();
        }
    }

	@Override
	public void run() {
        messageWindow = new MessageWindow(intersection, point);
		messageWindow.setVisible(true);
		
		super.run();
	}
}
