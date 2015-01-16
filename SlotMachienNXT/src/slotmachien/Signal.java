package slotmachien;

import slotmachien.actions.Action;

/**
 * Describes a generic signal that has a description and an action
 */
public class Signal {
    
    private String desc;
    private Action action;

    public Signal(String desc, Action action) {
        this.desc = desc;
        this.action = action;
    }

    public void performAction() {
        action.perform();
    }

    public String getDesc() {
        return desc;
    }
}
