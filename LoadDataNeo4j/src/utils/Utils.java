package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;

/**
 *
 * @author Thiago
 */
public class Utils {
    
    private static Driver driver;
    private static Session session;
    private static String quote ="'";
    private static String progLang;
    private static String followed;
    
    
    //Function that returns a list of files with txt extension
    public static List<String> getTxtFiles(String path){
        
        File[] files = new File(path).listFiles();
        List<String> txtFiles = new ArrayList<String>();;    
        
        for (File file : files) {
            try {
                if ((file.isFile()) && (getExt(file).equals("txt"))) {
                    txtFiles.add(file.getAbsolutePath());
                }
            } catch(Exception e){
                System.out.println("[Error] Searching files:" + e.getMessage());
            }
        }
             
        return txtFiles;
    }
    
    public static String getExt(File file){
        String fileName = file.getName();
        
        if ((fileName.lastIndexOf(".") != -1) && (fileName.lastIndexOf(".") != 0))
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else 
            return "";
    }
    
    // Function to connect on Neo4j and create a node called Github
    public static void connectNeo4j() 
    {      
        try
        {
            driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "admin" ) );
            session = driver.session();

            session.run( "CREATE (a:Platform {name: {name}})",
                    parameters( "name", "Github") );
            

        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }   
    }
    
    // Create a new node with the language name with a relationship called "Belongs" to Github node
    // quote variable is just to insert '
    public static void insertLangs(String lang)
    {
        // Ex: java_usersList.txt 
        // Using split function to get java
        File f = new File(lang);
        progLang = f.getName();
        String[] temp = progLang.split("_");
        progLang = temp[0];
        
        try 
        {  
            String query = "MATCH (p:Platform) WHERE p.name = 'Github' "
                    + "CREATE (m:Language {name : " + quote + progLang + quote +" } )-[:Belongs]->(p) ";
            
            session.run(query);
           
        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }            
    }
    
    // Function that receives a line from the file and insert a node with the user name and creates a relationship with a language or another user
    public static void insertUsers(String line)
    {
        String[] temp = line.split(";");
        
        followed = temp[0];
        String followedRepo = temp[2];
        String followedStars = temp[3];
        
        String follower = temp[4];
        String followerRepo = temp[6];
        String followerStars = temp[7];
        
        
        StatementResult result = session.run( "MATCH (a:Followed) WHERE a.name = {name} " +
                                      "RETURN a.name",
        parameters( "name", followed ) );
        
        // Testing if is a new user name to create a relationship with language node
        if (!result.hasNext()) 
        {
           session.run( "MATCH (l:Language) WHERE l.name =" + quote + progLang + quote
                   + " CREATE (a:Followed {name: {name}, stars: {stars}, repositories: {repo} }) -[:Use]->(l)",
           parameters( "name", followed, "stars", followedStars, "repo", followedRepo ) ); 
        }
        
        // Create a new node with the follower user name and a relationship with the followed
        session.run( "MATCH (f:Followed) WHERE f.name =" + quote + followed + quote
                   + " CREATE (a:Follower {name: {name}, stars: {stars}, repositories: {repo} }) -[:Follow]->(f)",
        parameters( "name", follower, "stars", followerStars, "repo", followerRepo ) );
           
    }
    
    // Closing connection
    public static void disconnectNeo4j()
    {
        session.close();
        driver.close();
    }
      
}
