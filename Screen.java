import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;//KeyListener, ActionListener, KeyEvent
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
//import java.util.ArrayList;
import java.util.Date;
//import java.lang.System;

public class Screen extends JPanel
implements KeyListener, MouseWheelListener, 
MouseListener, MouseMotionListener {
    Lab world;
    RunPhys run;
    
    static final int width = 768;//(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width*1/2;
    static final int height = 576;//(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height*1/2;
    
    public static double zRatio;//10^- //For LinkedParticle.0001 - 00000001;
    private BufferedImage back;
    public String stem = "Tests/00.";
    
    private int scrollamount = 0;
    public int frameCount = 0;
    
    public boolean onScreen;
    public static boolean recording = false;
    
    public int Width() {
        return (int)(width/zRatio);
    }
    public int Height() {
        return (int)(height/zRatio);
    }
    
    //Start of all Listeners' commands
    //keyListener commands
    public void keyReleased(KeyEvent e)
    {
        if(e.getKeyCode() == e.VK_SHIFT)
            world.shiftDown = false;
        if(e.getKeyCode() == e.VK_CONTROL)
            world.ctrlDown = false;
    }
    public void keyTyped(KeyEvent e){}
    public void keyPressed(KeyEvent e){
        int c = e.getKeyCode();
        
        switch(c)
        {
            case KeyEvent.VK_SHIFT: 
                world.shiftDown = true;
                break;
            case KeyEvent.VK_CONTROL: 
                world.ctrlDown = true;
                break;
            case KeyEvent.VK_RIGHT:
                /**
                 * trace 1: 1%10 +1 = 2
                 * trace 9: 9%10 +1 = 10
                 * trace 10: 10%10 +1 = 1
                 */
                world.density = (byte) (world.density%10 + 1);//1 to 10
                break;
            case KeyEvent.VK_LEFT:
                /**
                 * trace 1: (1-2)%10+1 = 10
                 * trace 2: (2-2)%10+1 = 1
                 * trace 10: (10-2)%10+1 = 9
                 */
                world.density = (byte) ((world.density - 2)%10 + 1);//1 to 10
                break;
            case KeyEvent.VK_UP:
                world.curserShape = (byte) ((world.curserShape + 1)%4);//0 to 3
                break;
            case KeyEvent.VK_DOWN:
                /**
                 * trace 0: (0-1)%4 = 3
                 * trace 1: (1-1)%4 = 0
                 * trace 3: (3-1)%4 = 2
                 */
                world.curserShape = (byte) ((world.curserShape-1)%4);//0 to 3
                break;
            case KeyEvent.VK_0:
                world.curserType = Types.FIXED;
                break;
            case KeyEvent.VK_1:
                world.curserType = Types.SAND;
                break;
            case KeyEvent.VK_2:
                world.curserType = Types.WATER;
                break;
            case KeyEvent.VK_3:
                world.curserType = Types.BOMB;
                break;
            case KeyEvent.VK_4:
                world.curserType = Types.BRICK;
                break;
            case KeyEvent.VK_5:
                world.curserType = Types.RUBBER;
                break;
            case KeyEvent.VK_SPACE:
                debugShout("Toggle Pause", 2);
                if(!run.paused()){
                    run.pause();
                }
                else if(run.paused()){
                    run.unpause();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                run.end();
                break;
            case KeyEvent.VK_R:
                if(recording)
                    recording = false;
                else
                {
                    frameCount = 0;
                    recording = true;
                }
                break;
            default:
                break;
        }
    }
    //MouseWheelListener command
    public void mouseWheelMoved(MouseWheelEvent e){
        scrollamount += e.getWheelRotation();  
        e.consume();
    }
    
    //A method useful for a MouseWheelListener to have
    public int getScroll(){
        int t = scrollamount;
        scrollamount = 0;
        if(t < 6)
            return t*t*t;
        else if(t < 11)
            return 75*t-250;
        else
            return 500;
    }
    
    //MouseListener commands
    public void mouseClicked(MouseEvent e){
        if(e.getButton() == MouseEvent.BUTTON1){
            debugShout("print (click)", 2);
            world.curser.setLocation((int)(e.getX()/zRatio), (int)(e.getY()/zRatio));
            world.print();
        }
        if(e.getButton() == MouseEvent.BUTTON3){
            debugShout("erase (click)", 2);
            world.curser.setLocation((int)(e.getX()/zRatio), (int)(e.getY()/zRatio));
            world.erase();
        }
    }
    public void mouseEntered(MouseEvent e){onScreen = true;}
    public void mouseExited(MouseEvent e){onScreen = false;}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    
    //MouseMotionListener commands
    public void mouseMoved(MouseEvent e){
        world.curser.setLocation((int)(e.getX()/zRatio), (int)(e.getY()/zRatio));}
    public void mouseDragged(MouseEvent e) {}
    
    /**
     * Constructor
     */
    public Screen(int endF, String saveFile, String SIM, double zoom, boolean start)
    {
        stem = saveFile;
        zRatio = zoom;
        
        System.out.print("Running Simulation: Save file= "+stem);
        recording = false;
        System.out.println(": Interactive Mode");
        addKeyListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        back = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        setPreferredSize(new Dimension(width, height));
        
        world = new Lab(this);
        
        world.sim(SIM);
        
        if(start){
            start();
        }
    }
    
    
    /**
     * Start. Also snap a frame zero
     */
    public void start(){
        run = new RunPhys(this);
        repaint();//Adds a frame zero (though frameCount = 1 for file format)
        if(recording)
            snap();
        Thread loop = new Thread(run);
        debugShout("Starting main loop", 1);
        loop.start();
    }
    /** 
     * Stop.
     */
    public void stop(){
        run.end();
        System.out.println("Simulation Complete");
    }
    
    public void paint(Graphics g)
    {
        //System.out.println("painting");
        g.setFont(new Font("font", Font.BOLD, 11));
        if(run.ended())
        {
            g.setFont(new Font("font", Font.BOLD, 30));
            g.setColor(Color.white);
            g.drawString("Simulation Complete", 10, world.getHeight()/ 2);
        }
        else
        {
            //Draw the black background
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
            /**
             * Make the top left corner display
             */
            g.setColor(Color.white);
            //g.drawString("fp's': "+20/world.time , 5, 15);
            g.drawString("simulation time: "+ 
                milTimeToStr(System.currentTimeMillis()-run.startTime), 5, 15);
            g.drawString("frame count: "+ frameCount, 5, 26);
            g.drawString("print density: "+ world.density, 5, 37);         
            g.drawString("zoom: "+ zRatio, 5, 48); 
            if(recording)
                g.drawString("recording  |  save destination: "+stem, 5, 59);
            else
                g.drawString("not recording", 5, 59);
            if(run.paused())
                g.drawString("PAUSED", 5, 70);
            else
                g.drawString("RUNNING", 5, 70);
            /**
             * Print the image of every being
             */
            for(BiList.Node n = world.beings.o1; n != null; n = n.getNext()){
                Being being = (Being) n.getVal();
                g.drawImage(being.getImage(), (int)(being.getDbX()*zRatio), (int)(being.getDbY()*zRatio), null);
            }
        }
    }
    public void snap(){
        debugShout("Frame "+frameCount+": "+
            milTimeToStr(System.currentTimeMillis()-run.startTime), 1);
        try{
            paint(back.getGraphics());
            File saveFile = new File(stem+(frameCount+1)+".png");
            ImageIO.write(back, "png", saveFile);
        }
        catch(IOException e){}
        saveSim();
    }
    public void saveSim(){
        try{
            File saveFile = new File(stem+frameCount+".phys");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(Execute.exportData(world.beings));
        }
        catch(IOException e){}
    }
    
    /**
     * Convert a time in milliseconds to a String
     * with the format "hh:mm:ss"
     */
    public static String milTimeToStr(long millis)
    {
        long sec = Math.round(millis/1000);
        long millm = millis%1000;
        long ss = sec % 60;
        long mm = (sec / 60) % 60;
        long hh = sec / 3600;
        return String.format("%02d:%02d:%02d:%04d", hh, mm, ss, millm);
    }
    
    public static void debugShout(String message, int depth){
        /* Generally: 0 = Once a sim
         * 1= once a frame
         * 2= multiple per frame
        */
        // How much to show - High depth messages only are printed if debugging is high
        if(Execute.debugging >= depth){
            System.out.println(message+ " at "+milTimeToStr(System.currentTimeMillis() - Execute.t0));
        }
    }
    public static void debugShout(String message){
        /* Default: 1*/
        if(Execute.debugging >= 1){
            System.out.println(message+ " at "+milTimeToStr(System.currentTimeMillis() - Execute.t0));
        }
    }
}