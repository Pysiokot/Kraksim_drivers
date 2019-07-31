package pl.edu.agh.cs.kraksim.real_extended;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.cs.kraksim.main.Simulation;
import pl.edu.agh.cs.kraksim.parser.ParsingException;

public class BlockedCellsInfo {
	private Integer firstCell;
	private Integer lastCell;
//	private Integer blockedLength; 
	//	both null or not
	private Integer turnStart; 
	private Integer turnEnd; 
//	private Integer turnDuration;
	
	public boolean isActive() {
		int turn = Simulation.turnNumber;
		return isActive(turn);
	}
	
	public boolean isActive(int turn) {
		if(turnStart == null) return turn>=0;	// time not set, not active before sim
		return (turnStart <= turn && turn <= turnEnd);
	}
	
	public boolean changedToInactiveThisTurn() {
		int turn = Simulation.turnNumber;
		return isActive(turn-1) && !isActive(turn);
	}
	
	public boolean changedToActiveThisTurn() {
		int turn = Simulation.turnNumber;
		return isActive(turn) && !isActive(turn-1);
	}
	
	public List<Integer> getCellIndexList(){
		List<Integer> cellsList = new ArrayList<>();
		for(int i=firstCell; i<=lastCell; i++) {
			cellsList.add(i);
		}
		return cellsList;
	}
	
	public String toString() {
		return String.format("blocked: [from cell %d to %d, in turns %d to %d]", firstCell, lastCell, turnStart, turnEnd);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	////	Builder and getters	
	public static class Builder {
		private Integer builderFirstCell;
		private Integer builderLastCell;
		private Integer builderBlockedLength; 
		private Integer builderTurnStart; 
		private Integer builderTurnEnd; 
		private Integer builderTurnDuration;
		
		
		public BlockedCellsInfo build() throws ParsingException {
			BlockedCellsInfo blockedInfo = new BlockedCellsInfo();
			blockedInfo.firstCell = this.builderFirstCell;
			// only 1 can be set: builderLastCell, builderBlockedLength
			if(builderLastCell == null && builderBlockedLength != null) {
				blockedInfo.lastCell = blockedInfo.firstCell + builderBlockedLength;
			}
			if(builderLastCell != null && builderBlockedLength == null) {
				blockedInfo.lastCell = builderLastCell;
			}
			if(builderLastCell == null && builderBlockedLength == null) {
				blockedInfo.lastCell = blockedInfo.firstCell;
			}
			
			blockedInfo.turnStart = this.builderTurnStart;
			// only 1 can be set: builderTurnEnd, builderTurnDuration
			if(builderTurnEnd == null && builderTurnDuration != null) {
				blockedInfo.turnEnd = blockedInfo.turnStart + builderTurnDuration;
			}
			if(builderTurnEnd != null && builderTurnDuration == null) {
				blockedInfo.turnEnd = builderTurnEnd;
			}
			if(builderTurnEnd == null && builderTurnDuration == null) {
				blockedInfo.turnEnd = blockedInfo.turnStart;
			}
			if(blockedInfo.firstCell == null || blockedInfo.lastCell == null 
					|| (blockedInfo.turnStart != null && blockedInfo.turnEnd == null)
					|| (blockedInfo.turnStart == null && blockedInfo.turnEnd != null)) {
				throw new ParsingException("Wrong blocked cells details params", null);
			}
			
			return blockedInfo;
		}
		
		public Builder firstCell(Integer builderFirstCell) {
			this.builderFirstCell = builderFirstCell;
			return this;
		}
		public Builder lastCell(Integer builderLastCell) {
			this.builderLastCell = builderLastCell;
			return this;
		}
		public Builder blockedLength(Integer builderBlockedLength) {
			this.builderBlockedLength = builderBlockedLength;
			return this;
		}
		public Builder turnStart(Integer builderTurnStart) {
			this.builderTurnStart = builderTurnStart;
			return this;
		}
		public Builder turnEnd(Integer builderTurnEnd) {
			this.builderTurnEnd = builderTurnEnd;
			return this;
		}
		public Builder turnDuration(Integer builderTurnDuration) {
			this.builderTurnDuration = builderTurnDuration;
			return this;
		}		
	}

	public static Builder builder() {
	    return new Builder();
	}
	
	public Integer getFirstCell() {
		return firstCell;
	}
	public Integer getLastCell() {
		return lastCell;
	}
	public Integer getTurnStart() {
		return turnStart;
	}
	public Integer getTurnEnd() {
		return turnEnd;
	}
}
