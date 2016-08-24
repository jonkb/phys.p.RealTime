import javax.swing.*;
import java.util.*;//stack, arraylist
public class RunPhys implements Runnable{
    private Screen mamma;
    public long startTime, prevTime;
    public int rate = 10;
    private static boolean end = false;
    private static boolean paused = false; 
    //How many are left to act
    //public static ArrayList<ActPhys> acting = new ArrayList<ActPhys>();
    public Queue<Thread> acting = new LinkedList<Thread>();
    public RunPhys(Screen mom){
        mamma = mom;
        startTime = System.currentTimeMillis();
    }
    public void pause(){
        paused = true;
        debugShout("Pausing.", 1);
    }
    public void unpause(){
        paused = false;
        debugShout("Unpausing.", 1);
    }
    public boolean paused(){
        return paused;
    }
    public void end(){
        end = true;
    }
    public boolean ended(){
        return end;
    }
    public void run(){
        debugShout("Simulation Started at: "+System.currentTimeMillis(), 0);
        prevTime = System.currentTimeMillis();
        debugShout("Wait a second.");
        try{
            Thread.sleep(rate);
        }catch(Exception e){System.out.println(e);}
        
        debugShout("Starting the main loop now");
        try{
            loop();
        }catch(Exception e){
            System.out.println("Exception at "+System.currentTimeMillis()+": "+e);
            throw e;
        }
        System.out.println("Simulation Ended at: "+System.currentTimeMillis());
    }
    private void loop(){
        while(!end){
            String f = "Frame: "+mamma.frameCount;
            if(paused)
                debugShout(f+": paused", 3);
            else
                debugShout(f);
            //Needed for keyboard input
            if (!mamma.onScreen)
                mamma.requestFocusInWindow();
            /**
             * Determines world.time based on the frame rate
             */
            //Time elapsed in seconds
            mamma.world.time = (double)((System.currentTimeMillis()-prevTime)/1000.0);
            prevTime = System.currentTimeMillis();
            assert mamma.world.time > 0;//You need to divide by world.time (dt) all the time
            
            if(paused){
                mamma.world.act();
            }
            else{
                
                mamma.frameCount++;
                mamma.world.act();
                /* New version implementing massive multithreading
                 */
                //act
                for(BiList.Node n = mamma.world.beings.a1; n != null; n = n.getNextA()){
                    Being being = (Being) n.getVal();
                    ActPhys a = new ActPhys(being, 0);
                    Thread t = new Thread(a);
                    acting.add(t);
                    t.start();
                }
                for(Thread t = acting.poll(); t!= null; t = acting.poll()){
                    try{
                        t.join();
                    }catch(Exception e){System.out.println(e);}
                }
                //updateXY
                for(BiList.Node n = mamma.world.beings.a1; n != null; n = n.getNextA()){
                    Being being = (Being) n.getVal();
                    ActPhys a = new ActPhys(being, 1);
                    Thread t = new Thread(a);
                    acting.add(t);
                    t.start();
                }
                for(Thread t = acting.poll(); t!= null; t = acting.poll()){
                    try{
                        t.join();
                    }catch(Exception e){System.out.println(e);}
                }
            }
            
            /* By invoking this later, it actually works. 
             * Repaint() must be called from EDT
             * Try switching it to a "synchronous" method of screen*/
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    //Everything needs to be in place before it is drawn on the canvas
                    for(Thread t = acting.poll(); t!= null; t = acting.poll()){
                        try{
                            t.join();
                        }catch(Exception e){System.out.println(e);}
                    }
                    mamma.repaint();
                    if(mamma.recording)
                        mamma.snap();
                }
            });
            
            try{
                Thread.sleep(rate);
            }catch(Exception e){System.out.println(e);}
        }
    }
    
    public static void debugShout(String message){
        Screen.debugShout(message);
    }
    public static void debugShout(String message, int p){
        Screen.debugShout(message, p);
    }
    
    class ActPhys implements Runnable{
        Being being;
        int version;
        /**
         * Make a new ActPhys Runnable of type act or updateXY
         * v(0) = act();
         * v(1) = updateXY();
         */
        public ActPhys(Being b, int v){
            being = b;
            assert v==0 || v==1;
            version = v;
            String to = "";
            if(v == 0)
                to = "to act";
            if(v == 1)
                to = "to move";
            debugShout(b+"\tAdd: "+to+": ("+acting.size()+" total)", 2);
        }
        public void run(){
            if(version == 0)
                being.act();
            if(version == 1)
                being.updateXY();
        }
    }
}