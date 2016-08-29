import javax.swing.*;

public class Test{
    public static void main(String args[]){
        System.gc();
        System.out.println("A");
        allTheThreads();
        System.out.println("B");
        inLine();
        System.out.println("C");
    }
    public static void biTest(){
        BiList<Being> list = new BiList<Being>();
        Sand s = new Sand();
        list.addA(s);
        shout("List: size="+list.size()+"; sizeA="+list.sizeA());
        shout("Removing s");
        list.remove(s);
        shout("List: size="+list.size()+"; sizeA="+list.sizeA());
        
        
        
        //Add 10 Fixed
        for(int i = 0; i<10; i++){
            list.add(new Fixed());
        }
        //Add 10 Sand
        for(int i = 0; i<10; i++){
            list.addA(new Sand());
        }
        Water w = new Water();
        list.addA(w);
        Fixed f = new Fixed();
        list.add(f);
        //Add 10 Fixed
        for(int i = 0; i<10; i++){
            list.add(new Fixed());
        }
        //Add 10 Sand
        for(int i = 0; i<10; i++){
            list.addA(new Sand());
        }
        
        shout("List: size="+list.size()+"; sizeA="+list.sizeA());
        shout("Removing w from the A list");
        list.remove(w);
        shout("List: size="+list.size()+"; sizeA="+list.sizeA());
        shout("Removing f from the B list");
        list.remove(f);
        shout("List: size="+list.size()+"; sizeA="+list.sizeA());
    }
    public static void errorTest(){
        double zero = 0;
        double one = 1.0;
        double ten = 10.0;
        double w = 768.0;
        System.out.println("Ulp of 0 is " + Math.ulp(zero));
        System.out.println("Ulp of 1 is " + Math.ulp(one));
        System.out.println("Ulp of 10 is " + Math.ulp(ten));
        System.out.println("Ulp of screen width is " + Math.ulp(w));
        
        
        double a = 10;
        double b = 10;
        double Esu = Math.ulp(a)+Math.ulp(b);
        System.out.println("Adding "+a+" and " +b+" with error of "+Esu+" gives "+(a+b));
        double Emu = (Math.ulp(a)/a+Math.ulp(b)/b)*(a*b);
        System.out.println("Multiplying "+a+" and " +b+" with error of "+Emu+" gives "+(a*b));
        
        a = 1000;
        b = 1000;
        Esu = Math.ulp(a)+Math.ulp(b);
        System.out.println("Adding "+a+" and " +b+" with error of "+Esu+" gives "+(a+b));
        Emu = (Math.ulp(a)/a+Math.ulp(b)/b)*(a*b);
        System.out.println("Multiplying "+a+" and " +b+" with error of "+Emu+" gives "+(a*b));
        
        
        a = 1000000;
        b = 1000000;
        Esu = Math.ulp(a)+Math.ulp(b);
        System.out.println("Adding "+a+" and " +b+" with error of "+Esu+" gives "+(a+b));
        Emu = (Math.ulp(a)/a+Math.ulp(b)/b)*(a*b);
        System.out.println("Multiplying "+a+" and " +b+" with error of "+Emu+" gives "+(a*b));
    }
    static int i = 0;
    static int running = 0;
    private static synchronized void incRunning(){
        running++;
    }
    private static synchronized void decRunning(){
        running--;
    }
    public static void allTheThreads(){
        System.gc();
        while(i < 5000){
            i++;
            Thread t = new Thread(new Do(i));
            t.start();
            //System.out.println(running+" Threads Running");
        }
    }
    public static void inLine(){
        i = 0;
        while(i < 5000){
            i++;
            //System.out.println(running+" Threads Running");
            System.out.println("Action number "+i+" executing. Time: "+System.currentTimeMillis());
            double b = 1;
            for(int a = 0; a< 10000; a++){
                b *= 1.01;
                if(b > Math.pow(10.11, 43.0))
                    System.out.print(b);
            }
            System.out.println("Action number "+i+" done");
        }
    }
    //Simply shortening print
    public static void shout(String s){
        System.out.println(s);
    }
    static class Do implements Runnable{
        public int n;
        public Do(int num){
            n = num;
        }
        public void run(){
            //incRunning();
            System.out.println("Thread number "+n+" executing. Time: "+System.currentTimeMillis());
            double b = 1;
            for(int a = 0; a< 10000; a++){
                b *= 1.01;
                if(b > Math.pow(10.11, 43.0))
                    System.out.print(b);
            }
            System.out.println("Thread number "+n+" done");
            //decRunning();
        }
    }
}
