package slotmachien.actions;

import lejos.nxt.LCD;
import slotmachien.SMMotorHandler;

public class DrawLcdAction extends Action {

    public static int XSIZE = 16;
    public static int YSIZE = 8;
    
    private SMMotorHandler smmh;
    
    public DrawLcdAction(SMMotorHandler smmh){
        this.smmh = smmh;
    }
    
    private void drawStatus(){
        String str = smmh.getDoorChangeDesc() + " " + smmh.getStatus().getAbbreviation();
        /*LCD.clear();
        LCD.drawString(
                str, 
                (XSIZE - str.length())/2, 
                (YSIZE)/2
                );
                */
        System.out.println(str);
    }

    @Override
    public void perform() {
        drawStatus();
    }

}
