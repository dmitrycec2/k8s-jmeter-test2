/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 321
 */
class TimedQueue extends Thread {
    private BlockingQueue<QueueElem> queue = new LinkedBlockingQueue();
    private final ConcurrentHashMap<String,Long> map = new ConcurrentHashMap<>();
    
    long waitTime = 0;
    
    public TimedQueue (long leaveTime) {
    this.waitTime = leaveTime;
    }
    
    
   public synchronized String checkValues(String str) {
       String[] arr = str.split(";");
       String res = "-1";
       System.out.println(arr.length);
       
       for (int i = 0; i < arr.length; i++) {
            if (!map.containsKey(arr[i])){
            res = i+"";
            map.put(arr[i], new Long(1));
            QueueElem elem = new QueueElem(arr[i], System.currentTimeMillis() + waitTime);
            queue.add(elem);
            break;
            }
       }
       System.out.println(res);
   return res;
   }
    
   @Override
   public void run(){
      while (true){
         long currenttime = System.currentTimeMillis();
          
         QueueElem elem = queue.peek();
         while ((elem!=null)&&(elem.checkTime(currenttime))) {
         map.remove(elem.getVal());
         queue.poll();
         elem = queue.peek();
         }
         
         
          try {
              sleep(waitTime);
          } catch (InterruptedException ex) {
              Logger.getLogger(TimedQueue.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
   } 
}
