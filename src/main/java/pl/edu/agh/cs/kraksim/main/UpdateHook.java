package pl.edu.agh.cs.kraksim.main;

import pl.edu.agh.cs.kraksim.ministat.CityMiniStatExt;

public interface UpdateHook {
	void onUpdate(CityMiniStatExt cityStat);
}
