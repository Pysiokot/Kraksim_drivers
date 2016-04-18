package pl.edu.agh.cs.kraksimcitydesigner.propertiesdialogs;

import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.cs.kraksimcitydesigner.element.Road;

// TODO: Auto-generated Javadoc
public class RoadEditableSpecification {
    Road road;
    
    String roadId;
    String roadStreetName;
    
    int uplinkNumOfLanes;
    int uplinkLength;
    List<Integer> uplinkRightLanes;
    List<Integer> uplinkLeftLanes;
    
    int downlinkNumOfLanes;
    int downlinkLength;
    List<Integer> downlinkRightLanes;
    List<Integer> downlinkLeftLanes;

    /**
     * Instantiates a new road editable specification.
     * 
     * @param road the road
     */
    public RoadEditableSpecification(Road road) {
        this.road = road;
        this.roadId = road.getId();
        this.roadStreetName = road.getStreet();
        
        this.uplinkNumOfLanes = road.getUplink().getNumberOfLanes();
        this.uplinkLength = road.getUplink().getLength();
        this.uplinkLeftLanes = new LinkedList<Integer>(road.getUplink().getLeftLanes());
        this.uplinkRightLanes = new LinkedList<Integer>(road.getUplink().getRightLanes());
        
        this.downlinkNumOfLanes = road.getDownlink().getNumberOfLanes();
        this.downlinkLength = road.getDownlink().getLength();
        this.downlinkLeftLanes = new LinkedList<Integer>(road.getDownlink().getLeftLanes());
        this.downlinkRightLanes = new LinkedList<Integer>(road.getDownlink().getRightLanes());
    }

    /**
     * Gets the road id.
     * 
     * @return the road id
     */
    public String getRoadId() {
        return roadId;
    }

    /**
     * Sets the road id.
     * 
     * @param roadId the new road id
     */
    public void setRoadId(String roadId) {
        this.roadId = roadId;
    }

    /**
     * Gets the road street name.
     * 
     * @return the road street name
     */
    public String getRoadStreetName() {
        return roadStreetName;
    }

    /**
     * Sets the road street name.
     * 
     * @param roadStreetName the new road street name
     */
    public void setRoadStreetName(String roadStreetName) {
        this.roadStreetName = roadStreetName;
    }

    /**
     * Gets the uplink num of lanes.
     * 
     * @return the uplink num of lanes
     */
    public int getUplinkNumOfLanes() {
        return uplinkNumOfLanes;
    }

    /**
     * Sets the uplink num of lanes.
     * 
     * @param uplinkNumOfLanes the new uplink num of lanes
     */
    public void setUplinkNumOfLanes(int uplinkNumOfLanes) {
        this.uplinkNumOfLanes = uplinkNumOfLanes;
    }

    /**
     * Gets the uplink length.
     * 
     * @return the uplink length
     */
    public int getUplinkLength() {
        return uplinkLength;
    }

    /**
     * Sets the uplink length.
     * 
     * @param uplinkLength the new uplink length
     */
    public void setUplinkLength(int uplinkLength) {
        this.uplinkLength = uplinkLength;
    }

    /**
     * Gets the uplink right lanes.
     * 
     * @return the uplink right lanes
     */
    public List<Integer> getUplinkRightLanes() {
        return uplinkRightLanes;
    }

    /**
     * Sets the uplink right lanes.
     * 
     * @param uplinkRightLanes the new uplink right lanes
     */
    public void setUplinkRightLanes(List<Integer> uplinkRightLanes) {
        this.uplinkRightLanes = uplinkRightLanes;
    }

    /**
     * Gets the uplink left lanes.
     * 
     * @return the uplink left lanes
     */
    public List<Integer> getUplinkLeftLanes() {
        return uplinkLeftLanes;
    }

    /**
     * Sets the uplink left lanes.
     * 
     * @param uplinkLeftLanes the new uplink left lanes
     */
    public void setUplinkLeftLanes(List<Integer> uplinkLeftLanes) {
        this.uplinkLeftLanes = uplinkLeftLanes;
    }

    /**
     * Gets the downlink num of lanes.
     * 
     * @return the downlink num of lanes
     */
    public int getDownlinkNumOfLanes() {
        return downlinkNumOfLanes;
    }

    /**
     * Sets the downlink num of lanes.
     * 
     * @param downlinkNumOfLanes the new downlink num of lanes
     */
    public void setDownlinkNumOfLanes(int downlinkNumOfLanes) {
        this.downlinkNumOfLanes = downlinkNumOfLanes;
    }

    /**
     * Gets the downlink length.
     * 
     * @return the downlink length
     */
    public int getDownlinkLength() {
        return downlinkLength;
    }

    /**
     * Sets the downlink length.
     * 
     * @param downlinkLength the new downlink length
     */
    public void setDownlinkLength(int downlinkLength) {
        this.downlinkLength = downlinkLength;
    }

    /**
     * Gets the downlink right lanes.
     * 
     * @return the downlink right lanes
     */
    public List<Integer> getDownlinkRightLanes() {
        return downlinkRightLanes;
    }

    /**
     * Sets the downlink right lanes.
     * 
     * @param downlinkRightLanes the new downlink right lanes
     */
    public void setDownlinkRightLanes(List<Integer> downlinkRightLanes) {
        this.downlinkRightLanes = downlinkRightLanes;
    }

    /**
     * Gets the downlink left lanes.
     * 
     * @return the downlink left lanes
     */
    public List<Integer> getDownlinkLeftLanes() {
        return downlinkLeftLanes;
    }

    /**
     * Sets the downlink left lanes.
     * 
     * @param downlinkLeftLanes the new downlink left lanes
     */
    public void setDownlinkLeftLanes(List<Integer> downlinkLeftLanes) {
        this.downlinkLeftLanes = downlinkLeftLanes;
    }
    
}
