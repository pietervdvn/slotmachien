package slotmachien.actions;

import slotmachien.SMMotorHandler;
import slotmachien.Signal;

public class SignalAction extends Action {

    private SMMotorHandler smmh;
    private Signal signal;

    public SignalAction(SMMotorHandler smmh, Signal signal) {
        this.smmh = smmh;
        this.signal = signal;
    }

    public void perform() {
        smmh.sendSignal(signal);
    }

}
