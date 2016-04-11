import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.BasicBSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.sun.javafx.collections.MappingChange.Map;

/**
 * Servlet implementation class TestPage
 */
@WebServlet("/TestPage")
public class TestPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestPage() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings({ "unused", "deprecation", "resource" })
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out= response.getWriter();
		out.println(" <head>\n"+
				" <meta charset=\"utf-8\">\n"+
				" <title>UCI ICS Search Engine</title>\n"+
				" <link rel=\"stylesheet\" href=\"css/style.css\">\n"+
				" <link href=\'http://fonts.googleapis.com/css?family=Lato:400,700\' rel=\'stylesheet\' type=\'text/css\'>\n"+
				" </head>");
		String word = request.getParameter("word");
		response.getWriter().append("Served at: ").append(request.getContextPath());
		//out.println("<h1>"+word+"</h1>");
		//BasicDBObject query = new BasicDBObject("token", new BasicDBObject("$regex", word));
		MongoClient indexClient = null;
		DB indexDb = null;
		DB textProcessor=null;
		indexClient = new MongoClient("localhost", 27017);
		indexDb = indexClient.getDB("InvertedIndex");
		//out.println("Connect to index database successfully");
		out.println("<h2>The Results for the query "+word+" are: </h2>");
		out.println("<br>");
		out.println("<br>");
		//DB database = Db.connectToIndexerDb();
		
		String searchQuery = word;
		searchQuery = searchQuery.toLowerCase();
		//HashMap<String, Double> urlMap = new HashMap<>();
		Scanner scanner = new Scanner(searchQuery);
		while (scanner.hasNext()) {
			String s = scanner.next();
			HashMap<String, Double> temp = new HashMap<>();
			//System.out.println(s);
			BasicDBObject query = new BasicDBObject("token", new BasicDBObject(
					"$regex", s));
			//temp = index.getData("details", query);	
				ArrayList<String> resultArray = new ArrayList<String>();
				//DB database = Db.connectToIndexerDb();
				HashMap<String, Double> urlMap = new HashMap<>();
				if(indexDb != null){
					DBCollection collection = indexDb.getCollection("data");		
					DBCursor cursor = collection.find(query);			
					while (cursor.hasNext()) {
						String c = cursor.next().toString();
						Object obj = JSON.parse(c);
						JSONObject json = new JSONObject(((BasicBSONObject) obj));
						JSONArray jsonArray = null;
						try {
							jsonArray = json.getJSONArray("details");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (int i = 0; i < jsonArray.length(); i++) {
						    JSONObject jsonobject = null;
							try {
								jsonobject = jsonArray.getJSONObject(i);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    String url = null;
							try {
								url = jsonobject.getString("url");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    double tfIdf = 0;
							try {
								tfIdf = (double)jsonobject.get("tfIdf");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    urlMap.put(url, tfIdf);
						}
						String dataOption="details";
						String result = ((BasicBSONObject) obj).getString(dataOption);			
					}
				}	
				temp = urlMap;
			
			for (Entry<String, Double> entry : temp.entrySet()) {
	            String key = entry.getKey();
	            Double value = entry.getValue();
	            if (urlMap.get(key) == null){
	            	urlMap.put(key, value);
	            }else{
	            	double newValue = urlMap.get(key)+value;
	            	urlMap.put(key, newValue);
	            }
	        } 			
		
		scanner.close();
		ArrayList<String> topTenUrlslist = new ArrayList<>();
		TreeMap<String, Double> sortedUrlMap;
		ValueComparator vc = new ValueComparator(urlMap);
        TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(vc);
        sortedMap.putAll(urlMap);
        sortedUrlMap = sortedMap;
    
    
    
        
        
		
		for (Entry<String, Double> entry : sortedUrlMap.entrySet()) {
			 if (topTenUrlslist.size() > 9) break;
			 topTenUrlslist.add(entry.getKey());
			 System.out.println(entry.getValue());
		}
		//out.println(topTenUrlslist);
		for (String str:topTenUrlslist){
			out.println("<div><a href="+str+">"+str+"</a></div>");
			BasicDBObject query1 = new BasicDBObject("url", new BasicDBObject("$regex", str));				
		        ArrayList<String> resultArray1 = new ArrayList<String>();		
		        MongoClient crawlerClient = new MongoClient("localhost", 27017);
		        DB crawlerDb = crawlerClient.getDB("Crawler");
		        if(crawlerDb != null){
			        DBCollection collection = crawlerDb.getCollection("WebDocs");			
			        DBCursor cursor = collection.find(query1);
			        while (cursor.hasNext()) {
				        String c = cursor.next().toString();
				        Object obj = JSON.parse(c);
				        String result = ((BasicBSONObject) obj).getString("text");
				        //System.out.println(result);				
				        resultArray1.add(result.substring(0, Math.min(0 + 100, result.length())).replace("\n", " " ).concat("...."));
				        System.out.println(resultArray1);	
			        }
		        }	
			out.println("<br>");
			out.println("<div>"+resultArray1.toString()+"</div>");
			out.println("<br><br>");
		}
		}
			
		}
}