package pl.edu.agh.cs.kraksim.visual.infolayer;

import pl.edu.agh.cs.kraksim.iface.block.BlockIView;
import pl.edu.agh.cs.kraksim.iface.carinfo.CarInfoIView;
import pl.edu.agh.cs.kraksim.iface.eval.EvalIView;
import pl.edu.agh.cs.kraksim.main.EvalModuleProvider;
import pl.edu.agh.cs.kraksim.ministat.MiniStatEView;

/**
 * User: mhareza
 */
public class InfoProvider {

    private BlockIView blockView;
    private EvalModuleProvider evalProvider;
    private MiniStatEView statView;
    private EvalIView evalIView;

    public void setEvalIView(EvalIView evalIView) {
        this.evalIView = evalIView;
    }

    public EvalIView getEvalIView() {
        return evalIView;
    }

    public CarInfoIView getCarInfoIView() {
        return carInfoIView;
    }

    public void setCarInfoIView(CarInfoIView carInfoIView) {
        this.carInfoIView = carInfoIView;
    }

    private CarInfoIView carInfoIView;


    private static InfoProvider ourInstance = new InfoProvider();

    public static InfoProvider getInstance() {
        return ourInstance;
    }

    private InfoProvider() {
    }

    public void setBlockView(BlockIView blockView) {
        this.blockView = blockView;
    }

    public BlockIView getBlockView() {
        return blockView;
    }

    public void setEvalProvider(EvalModuleProvider evalProvider) {
        this.evalProvider = evalProvider;
    }

    public EvalModuleProvider getEvalProvider() {
        return evalProvider;
    }

    public MiniStatEView getStatView() {
        return statView;
    }

    public void setStatView(MiniStatEView statView) {
        this.statView = statView;
    }
}
