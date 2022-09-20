/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import net.minidev.json.JSONObject;
/**
 *
 * @author 321
 */
public class HTTPserver {

    public HTTPserver() throws IOException {
        //System.out.println(new File(".").getAbsolutePath());
        //String response=readFile("./index.html");
    }

    
    private static final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, BlockingQueue> listMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, BlockingQueue> queueMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TimedQueue> TimedQueue = new ConcurrentHashMap<>();
    
    private static int threadCount = 50;
    private static int port = 8089;
    
    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            try {
                threadCount = Integer.parseInt(args[0]);
                port = Integer.parseInt(args[1]);
            } catch(NumberFormatException e){
            System.out.println("thread and port should be integer");
            }
        }
        HTTPserver A = new HTTPserver();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
        server.createContext("/valueget", new ValueGet());
        server.createContext("/valueput", new ValuePut());
        server.createContext("/valuepoll", new ValuePoll());
        server.createContext("/listoffer", new ListOffer());
        server.createContext("/listpoll", new ListPoll());
        server.createContext("/queueoffer", new Queueoffer());
        server.createContext("/queuepoll", new QueuePoll());
        server.createContext("/blockedqueue", new BlockedQueue());
        server.createContext("/", new Index());
        server.createContext("/listinfo", new ListInfo());
        server.createContext("/valueinfo", new ValueInfo());
		
        Executor exec = Executors.newFixedThreadPool(threadCount);
        server.setExecutor(exec);
        server.start();
    }

    private static String readFile(String fileName) throws FileNotFoundException, IOException {
       BufferedReader br=new BufferedReader(new FileReader(fileName));
			String temp=br.readLine();
			String s="";
			while(temp!=null){
				s+=temp;
				temp=br.readLine();
			}
			return s;
    }

    private static class ValueGet implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)"))){
               response = "format: http://{host}:{port}/valueget?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)", "$1");
               response = (map.get(param) == null) ? "" : map.get(param);
           }
           
           he.sendResponseHeaders(200, response == null ? 0 : response.length());
           try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           }
        }
    }

    private static class ValuePut implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)&value=(.*)"))){
               response = "format: http://{host}:{port}/valueget?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)&value=(.*)", "$1");
               String value = query.replaceAll("param=(.+)&value=(.*)", "$2");
                // String param = query.replaceAll("param=(.+)&", "$1");
                // String value = query.replaceAll("&value=(.*)", "$2");
               map.put(param, value);
               response = "ok"; 
               
               
           }
		   he.sendResponseHeaders(200, response == null ? 0 : response.length());
                      try (OutputStream os = he.getResponseBody()){
						  he.sendResponseHeaders(200, 0);
               os.write(response.getBytes());
           } 
        }
    }

    private static class ValuePoll implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)"))){
               response = "format: http://{host}:{port}/valueget?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)", "$1");
               response = (map.get(param) == null) ? "" : map.get(param); 
               map.remove(param);
           }
           he.sendResponseHeaders(200, response == null ? 0 : response.length());
           try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           }           
        }
    }



    private static class ListOffer implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)&value=(.*)"))){
               response = "format: http://{host}:{port}/valueget?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)&value=(.*)", "$1");
               String value = query.replaceAll("param=(.+)&value=(.*)", "$2");
               BlockingQueue queue = listMap.get(param);
              if (queue == null) {
              queue = new LinkedBlockingQueue();
              listMap.put(param, queue);              
              }
              queue.offer(value);
              response = "ok";
           }
           
           he.sendResponseHeaders(200, response == null ? 0 : response.length());
               try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           } 
        }
    }

    private static class ListPoll implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)"))){
               response = "format: http://{host}:{port}/listpoll?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)", "$1");
               BlockingQueue queue = listMap.get(param);
               if (queue != null) {
               response = (String) queue.poll();
               response = (response == null) ? "" : response;
                                      }
           } 

           he.sendResponseHeaders(200, response == null ? 0 : response.length());
               try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           } 
          
        }
    }


 	private static class ValueInfo
    implements HttpHandler
  {
    public void handle(HttpExchange he)
      throws IOException
    {
      String response = "";
      String val;
      
      JSONObject obj = new JSONObject();
      for (Iterator localIterator = HTTPserver.map.entrySet().iterator(); localIterator.hasNext();)
      {
          Map.Entry queue = (Map.Entry)localIterator.next();
        if (queue != null)
        {
          val = (String)queue.getValue();
          obj.put(queue.getKey().toString(), val);
       
        }
      }
      he.sendResponseHeaders(200, response == null ? 0L : response.length());
      OutputStream os = he.getResponseBody();Map.Entry<String, BlockingQueue> queue = null;
      try
      {
        os.write(obj.toJSONString().getBytes());
      }
      catch (Throwable localThrowable1)
      {
        
      }
      finally
      {
        if (os != null) {
          /*if (queue != null) {
            try
            {
              os.close();
            }
            catch (Throwable localThrowable2)
            {
              
            }
          } else {
            os.close();
          }*/
        	os.close();
        	
        }
      }
    }
  }   
	private static class ListInfo
    implements HttpHandler
  {
    public void handle(HttpExchange he)
      throws IOException
    {
      String response = "";
      Integer cursize = Integer.valueOf(0);
      
      JSONObject obj = new JSONObject();
      for (Iterator localIterator = HTTPserver.listMap.entrySet().iterator(); localIterator.hasNext();)
      {
          Map.Entry queue = (Map.Entry)localIterator.next();
        if (queue != null)
        {
          cursize = Integer.valueOf(((BlockingQueue)queue.getValue()).size());
          obj.put(queue.getKey().toString(), cursize.toString());
       
        }
      }
      he.sendResponseHeaders(200, response == null ? 0L : response.length());
      OutputStream os = he.getResponseBody();Map.Entry<String, BlockingQueue> queue = null;
      try
      {
        os.write(obj.toJSONString().getBytes());
      }
      catch (Throwable localThrowable1)
      {
        
      }
      finally
      {
        if (os != null) {
          if (queue != null) {
            try
            {
              os.close();
            }
            catch (Throwable localThrowable2)
            {
              
            }
          } else {
            os.close();
          }
        }
      }
    }
  }
	
    private static class Queueoffer implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           
           try {
           if ((query == null) || (!query.matches("param=(.+)&value=(.*)&wait=(\\d+)"))) {
               response = "format: http://{host}:{port}/queueoffer?param={param}&value={value}&wait={waittime}";
              }else{
               String param = query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$1");
               String value = query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$2");
               Long waitTime = Long.parseLong(query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$3"));
               
               BlockingQueue queue = queueMap.get(param);
              if (queue == null) {
              queue = new LinkedBlockingQueue();
              queueMap.put(param, queue);              
              }
              
              long time = System.currentTimeMillis();
              QueueElem elem = new QueueElem(value, time+waitTime);
              queue.offer(elem);
              response = "ok";
              }
               he.sendResponseHeaders(200, response == null ? 0 : response.length());
               try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           }
           
           } catch(NumberFormatException e){
           response = "Can not parse waitTime";
               he.sendResponseHeaders(200, response == null ? 0 : response.length());
               try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           }
           }
           
            
        }
    }

	
	
	
	
    private static class QueuePoll implements HttpHandler {

   private synchronized String getElem(BlockingQueue queue){  
                   String resp = null;
           QueueElem elem = (QueueElem) queue.peek();
           if (elem == null) return resp;
           if (elem.checkTime(System.currentTimeMillis())) {
               resp = elem.getVal();
               queue.poll();
           }
           return resp;
   }
        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8");
           if ((query == null) || (!query.matches("param=(.+)"))){
               response = "format: http://{host}:{port}/queuepoll?param={param}";
           } else {
               String param = query.replaceAll("param=(.+)", "$1");
               BlockingQueue queue = queueMap.get(param);
               if (queue != null) {
               response = this.getElem(queue);
               response = (response == null) ? "" : response;
                                      }
           } 
               he.sendResponseHeaders(200, response == null ? 0 : response.length());
               try (OutputStream os = he.getResponseBody()){
               os.write(response.getBytes());
           }           
           
        }
    }

    private static class BlockedQueue implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
           String response = "";
           String query = URLDecoder.decode(he.getRequestURI().getQuery(), "UTF-8"); 
           try {
           if ((query == null) || (!query.matches("param=(.+)&value=(.*)&wait=(\\d+)"))) {
               response = "format: http://{host}:{port}/blockedqueue?param={param}&value={value}&wait={waittime}";
              }else{
               String param = query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$1");
               String value = query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$2");
               Long waitTime = Long.parseLong(query.replaceAll("param=(.+)&value=(.*)&wait=(\\d+)", "$3"));
           }
        }catch(Exception e){}
    }
    }
    private static class Index implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

            //Writer w = new OutputStreamWriter(he.getResponseBody(), "UTF-8");
//he.getResponseHeaders().put("Content-Type", Arrays.asList("text/plain; charset=UTF-8"));
he.sendResponseHeaders(200, 0);

            String response=readFile("./index.html");
            //System.out.println(response);

            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
//w.write(response);
//w.close();
        }
        
        
        
        		private static String readFile(String fileName) throws IOException
		{
			BufferedReader br=new BufferedReader(new FileReader(fileName));
			String temp=br.readLine();
			String s="";
			while(temp!=null){
				s+=temp;
				temp=br.readLine();
			}
			return s;
		}
    }
    
}
