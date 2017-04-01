package loaddataneo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;


/**
 *
 * @author Thiago
 */
public class LoadDataNeo4j {
    
    public static void main(String[] args) {
        
        //filesPath is the path of txt files from GitHubCrawler project
        String filesPath = args[0];
        
        List<String> folderFiles = new ArrayList<String>();
        
        //folderFiles is a list of files
        folderFiles = Utils.getTxtFiles(filesPath);
        
        String line;
        
        // Neo4j connection
        Utils.connectNeo4j();
        
        for (int i=0;i<folderFiles.size();i++)
        {
            // Insert a node with the programming language name and creates a relationship with Github node
            Utils.insertLangs(folderFiles.get(i));
            int counter = 0;
            
            try {
                BufferedReader br = new BufferedReader(new FileReader(folderFiles.get(i)));
                
                 while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    
                    // Ignoring header
                    if (counter>0) {
                        // Inserting follower and followed
                        Utils.insertUsers(line);
                    }
                    
                    counter++;
                 } 
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }    
        }
        
        // Neo4j disconnection
        Utils.disconnectNeo4j();
        
    }
    
}
