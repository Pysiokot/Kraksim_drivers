package pl.edu.agh.cs.kraksim.weka.utils;

import pl.edu.agh.cs.kraksim.weka.data.LinkInfo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Neighbours implements Serializable {
	private static final long serialVersionUID = -8259341726909583298L;
	public SortedSet<LinkInfo> roads = new TreeSet<>();
	public Set<String> intersections = new HashSet<>();
}
