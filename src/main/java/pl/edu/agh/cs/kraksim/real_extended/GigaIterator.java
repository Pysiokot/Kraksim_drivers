package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.core.Lane;
import pl.edu.agh.cs.kraksim.core.Link;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class GigaIterator implements Iterator {

	private ArrayList<ListIterator<Car>> carsOnLanes = new ArrayList<>();

	public GigaIterator(Link link, RealEView ev){
		Lane[] lanes = link.getLanes();
		for (Lane lane : lanes) {
			carsOnLanes.add(ev.ext(lane).getCarsIterator());
		}
	}


	@Override
	public boolean hasNext() {
		for(ListIterator<Car> iterator : carsOnLanes){
			if(iterator.hasNext()){
				return true;
			}
		}
		return false;
	}

	@Override
	public Car next() {
		int i = Integer.MAX_VALUE;
		ListIterator<Car> it = null;
		for(ListIterator<Car> iterator : carsOnLanes){
			if(iterator.hasNext()){
				//return iterator.next();
				Car car = iterator.next();
				if(car.getPosition() < i){
					i = car.getPosition();
					it = iterator;
				}
				iterator.previous();
			}
		}
		if(it != null) {
			
			return it.next();
		}
		return null;
	}

	@Override
	public void remove() {
	}
}
