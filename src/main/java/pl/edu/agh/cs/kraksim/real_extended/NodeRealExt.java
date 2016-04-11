package pl.edu.agh.cs.kraksim.real_extended;

import pl.edu.agh.cs.kraksim.iface.block.NodeBlockIface;

abstract class NodeRealExt implements NodeBlockIface {
	protected final RealEView ev;

	NodeRealExt(RealEView ev) {
		this.ev = ev;
	}
}
