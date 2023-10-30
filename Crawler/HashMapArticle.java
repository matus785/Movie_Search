package Crawler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.io.*;

public class HashMapArticle extends HashMap<Integer, Article>
{
    public HashMapArticle(int initialCapacity)
    {
        new HashMap<Integer, Article>(initialCapacity, 0.8f);
    }

    public HashMapArticle()
    {
        new HashMap<Integer, Article>(50, 0.8f);
    }

    public boolean updateLinks(Integer key, ArrayList<URI> links, Integer sourceDepthPlus)
    {
        Article old = this.remove(key);
        if (old == null) return false;

        for (URI u : links) old.addSource(u, sourceDepthPlus);

        if (this.put(key, old) != null) return false;
        return true;
    }

    public Article getMostLinked()
    {
        Article best = null;
        Integer foundID = 0;
        Integer maxLinks = 0;
        Integer minDepth = -1;

        //FIFO depending on priority (link count and depth)
        for (Map.Entry<Integer, Article> a : this.entrySet())
        {
            Article current = a.getValue();
            Integer currentLinks = current.linkedBy.size();

            //Found domain
            if (currentLinks == 0)
            {
                best = current;
                foundID = a.getKey();
                break;
            }
            //Prioritize bigger link count first and smaller depth second
            if (maxLinks < currentLinks || (maxLinks.equals(currentLinks) && minDepth > current.depth))
            {
                best = current;
                foundID = a.getKey();
                maxLinks = currentLinks;
                minDepth = current.depth;
            }
        }

        if (best != null) return this.remove(foundID);

        return null;
    }

    public void writeHashMap(String filePath) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(filePath, false);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(this);

        oos.close();
        fos.close();
    }

    public static HashMapArticle readHashMap (String filePath) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(fis);

        HashMapArticle articles = (HashMapArticle) ois.readObject();

        ois.close();
        fis.close();

        return articles;
    }

}
