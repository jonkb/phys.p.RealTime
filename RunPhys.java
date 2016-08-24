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
    public Stack<Runnable> acting = new Stack<Runnable>();
    private int laggers = 0;//, a=0;
    private Stack<Runnable> toDo = new Stack<Runnable>();
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
        
        debugShout("Actually starting main loop");
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
                mamma.world.curser.act();
            }
            else{
                assert acting.size() == 0: acting;
                
                mamma.frameCount++;
                mamma.world.act();
                /* New version implementing massive multithreading
                 */
                for(BiList.Node n = mamma.world.beings.a1; n != null; n = n.getNextA()){
                    Being being = (Being) n.getVal();
                    ActPhys a = new ActPhys(being, 0);
                    //acting.add(a);
                    acting.push(a);
                    Thread t = new Thread(a);
                    t.start();
                }
                //Wait for everybody to finish applying forces
                while(!acting.empty()){//acting.size()>0)
                    debugShout("Waiting for actors to act");
                    ActPhys a = (ActPhys)acting.pop();
                    a.end();
                }
                assert acting.size() == 0: acting;
                for(BiList.Node n = mamma.world.beings.a1; n != null; n = n.getNextA()){
                    Being being = (Being) n.getVal();
                    ActPhys a = new ActPhys(being, 1);
                    acting.add(a);
                    Thread t = new Thread(a);
                    t.start();
                }
            }
            
            /* By invoking this later, it actually works. 
             * Repaint() must be called from EDT*/
            holdUp();
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    //Everything needs to be in place before it is drawn on the canvas
                    while(!acting.empty()){//acting.size()>0)
                        debugShout("Waiting for actors to move");
                        ActPhys a = (ActPhys)acting.pop();
                        a.end();
                    }
                    mamma.repaint();
                    if(mamma.recording)
                        mamma.snap();
                    thanks();
                }
            });
            
            /* Hold up for things that need a moment.
             * Allow invokeLater threads to run
             * Prevent ConcurrentModification
             */
            while(laggers > 0){//holdUp() RunPhys:[acting.move-repaint]
                try{
                    debugShout("I'm Waiting.", 3);
                    Thread.sleep(10);
                }catch(Exception e){System.out.println(e);}
            }
            while(!toDo.empty()){//doLater() Lab:[print, erase, dumpBin]
                debugShout("Running a postponed action", 2);
                Runnable next = toDo.pop();
                next.run();
            }
            
            try{
                Thread.sleep(rate);
            }catch(Exception e){System.out.println(e);}
        }
    }
    
    /**
     * Called by any function that could take longer than 20millis
     * but needs to run while RunPhys is not iterating.
     * Prevents ConcurrentModification with invokeLater on EDT
     */
    public void holdUp(){
        laggers++;
    }
    /**
     * Signify that the action is done
     */
    public void thanks(){
        assert laggers > 0;
        laggers--;
    }
    /**
     * An alternative interface that doesn't use holdUp()
     * These Runnables are executed by the RunPhys Thread, not EDT
     */
    public void doLater(Runnable todo){
        toDo.push(todo);
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
        boolean done = false;
        /**
         * Make a new ActPhys Runnable of type act or updateXY
         * v(0) = act();
         * v(1) = updateXY();
         */
        public ActPhys(Being b, int v){
            being = b;
            assert v>-1 && v<2;
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
            done = true;
        }
        public void end(){
            while(!done){
                try{
                    Thread.sleep(10);
                }catch(Exception e){System.out.println(e);}
            }
            acting.remove(this);
            debugShout(being+"\tDone ("+acting.size()+" left)", 2);
        }
    }
}