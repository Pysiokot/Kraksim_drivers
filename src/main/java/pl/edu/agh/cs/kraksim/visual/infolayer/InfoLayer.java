package pl.edu.agh.cs.kraksim.visual.infolayer;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import pl.edu.agh.cs.kraksim.core.Gateway;
import pl.edu.agh.cs.kraksim.core.Intersection;
import pl.edu.agh.cs.kraksim.core.Link;
import pl.edu.agh.cs.kraksim.ministat.GatewayMiniStatExt;
import pl.edu.agh.cs.kraksim.ministat.LinkMiniStatExt;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;
import pl.edu.agh.cs.kraksim.visual.infolayer.messages.MessageWindowThreadFactory;
import pl.edu.agh.cs.kraksim.visual.infolayer.messages.NodeMessageWindowThread;

/**
 * Model warstwy odpowiadajÄ…cej za wyĹ›wietlanie klikalnych kwadracikĂłw na mapie
 * umoĹĽliwia wyĹ›wietlanie dodatkowych statystyk dla konkretnej drogi
 * 
 * operacje na mapie synchroniczne w celu unikniÄ™cia jednoczesnej modyfikacji mapy przez kilka wÄ…tkĂłw
 * @author borowski
 *
 */
public class InfoLayer {

	/**
	 * Mapa odwzorowywujÄ…ca prostokÄ…t w konkretny link i na odwrĂłt
	 */
	private BidiMap<Rectangle2D, Link> bidiLinkMap;
	
	/**
	 * Mapa odwzorowywujÄ…ca prostokÄ…t w konkretny intersection i na odwrĂłt
	 */
	private BidiMap<Rectangle2D, Intersection> bidiIntersectionMap;
	
	/**
	 * Mapa odwzorowywujÄ…ca prostokÄ…t w konkretny gateway i na odwrĂłt
	 */
	private BidiMap<Rectangle2D, Gateway> bidiGatewayMap;
	
	private MiniStatEView statView;
    private List<NodeMessageWindowThread> nodeMessageWindowThreadList;

    public InfoLayer(MiniStatEView statView) {
		this.statView = statView;
		bidiLinkMap = new DualHashBidiMap<Rectangle2D, Link>();
		bidiIntersectionMap = new DualHashBidiMap<Rectangle2D, Intersection>();
		bidiGatewayMap = new DualHashBidiMap<Rectangle2D, Gateway>();
        nodeMessageWindowThreadList = new ArrayList<NodeMessageWindowThread>();
	}

	/**
	 * Metoda prĂłbuje znaleĹşÄ‡ w mapach dany punkt i odpowiednio wyĹ›wietliÄ‡ w okienku szczegĂłĹ‚owe informacje
	 * {@link pl.edu.agh.cs.kraksim.visual.VisualizatorComponent}
	 * @param x - wspĂłĹ‚rzÄ™dna x klikniÄ™cia
	 * @param y
	 * @param scale - skala podawana przez zoom slidera 
	 * @see pl.edu.agh.cs.kraksim.visual.VisualizatorComponent
	 */
	synchronized public void showInfoMessageWindow(double x, double y, float scale){
		Point p = new Point(Double.valueOf(x).intValue(), Double.valueOf(y).intValue());
		Link link = tryFindLink(x, y, scale);
		if(link != null){
			LinkMiniStatExt miniStat = statView.ext(link);
			
			MessageWindowThreadFactory.create(link, miniStat, p).start();
			return;
		}
		
		Intersection intersection = tryFindIntersection(x, y, scale);
		if(intersection != null){
            NodeMessageWindowThread messageWindowThread = (NodeMessageWindowThread) MessageWindowThreadFactory.create(intersection, p);
            messageWindowThread.start();
            nodeMessageWindowThreadList.add(messageWindowThread);
			return;
		}
		
		Gateway gateway = tryFindGateway(x, y, scale);
		if(gateway != null){
			GatewayMiniStatExt gatewayMiniStatExt = statView.ext(gateway);
			MessageWindowThreadFactory.create(gateway, gatewayMiniStatExt, p).start();
		}
		
	}
	
	
	synchronized private Link tryFindLink(double x, double y, float scale){
		return tryFindInMap(bidiLinkMap, x, y, scale);
	}
	
	synchronized private Intersection tryFindIntersection(double x, double y, float scale){
		return tryFindInMap(bidiIntersectionMap, x, y, scale);
	}
	
	synchronized private Gateway tryFindGateway(double x, double y, float scale){
		return tryFindInMap(bidiGatewayMap, x, y, scale);
	}
	
	synchronized private <T> T tryFindInMap(BidiMap<Rectangle2D, T> bidiMap, double x, double y, float scale){
		for(Rectangle2D rect: bidiMap.keySet()){
			if(rect.contains(x / scale, y / scale ))
				return bidiMap.get(rect);
		}
		return null;
	}
	
	synchronized public void putLink(Rectangle2D key, Link value){
		bidiLinkMap.put(key, value);
	}
	
	synchronized public Rectangle2D getRectangleForLink(Link key){
		return bidiIntersectionMap.getKey(key);
	}
	
	synchronized public void putIntersection(Rectangle2D key, Intersection value){
		bidiIntersectionMap.put(key, value);
	}
	
	synchronized public Rectangle2D getRectangleForIntersection(Intersection key){
		return bidiLinkMap.getKey(key);
	}
	
	synchronized public void putGateway(Rectangle2D key, Gateway value){
		bidiGatewayMap.put(key, value);
	}
	
	synchronized public Rectangle2D getRectangleForGateway(Gateway key){
		return bidiGatewayMap.getKey(key);
	}


    public void updateIntersectionMessageBox() {
        for (NodeMessageWindowThread nodeMessageWindowThread : nodeMessageWindowThreadList) {
            if (nodeMessageWindowThread != null){
                nodeMessageWindowThread.update();
            }
        }
    }
}
