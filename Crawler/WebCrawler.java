package Crawler;

import Crawler.Domains.Domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class WebCrawler
{
    private static final String directoryPath = System.getProperty("user.dir") + "\\src\\Crawler\\";
    private static final String visitedPath = directoryPath + "dataVisited.txt";
    private static final String notVisitedPath = directoryPath + "dataNotVisited.txt";


    private ArrayList<Domain> domains = new ArrayList<>();
    private HashMapArticle visited = new HashMapArticle();
    private HashMapArticle notVisited = new HashMapArticle();

    public WebCrawler(String[] domains)
    {
        this.loadCrawler();

        for (String str : domains)
        {
            try
            {
                Domain d = new Domain(str, directoryPath);
                this.domains.add(d);

                URI uri = new URI(str);
                if (!visited.containsKey(uri.hashCode()))
                {
                    Article a = new  Article(uri, d, 0);

                    this.notVisited.put(a.getID(), a);
                }
            }
            catch (Exception e) {
                //TODO log
                //e.printStackTrace();
                System.out.println("ERROR: Domain '" + str + "' NOT correct!" );
            }
        }
    }

    public void crawl (Integer limit)
    {
        long startTime = 0;
        String lastDomain = null;
        //TODO check to revisit articles

        while (limit > 0 && !this.notVisited.isEmpty())
        {
            try
            {
                //for (Map.Entry<Integer, Article> a : this.notVisited.entrySet()) System.out.println(a.getValue().linkedBy.size() + " | " + a.getValue().depth + " > " + a.getValue().uri);

                //Get the highest priority article
                Article currentArticle = this.notVisited.getMostLinked();
                if (currentArticle == null) throw new NoSuchElementException("Article NOT found");
                if (currentArticle.isVisited()) throw new RuntimeException("Article already visited");

                //Delay crawl if needed
                if (lastDomain != null && lastDomain.equals(currentArticle.host.name)) crawlWait(startTime, currentArticle.host.getDelay());

                //Set last domain
                lastDomain = currentArticle.host.name;
                startTime = System.currentTimeMillis();

                //Download article
                //TODO log
                System.out.println("HTTP/GET: " + currentArticle.uri);
                currentArticle.visitArticle();
                Integer id = currentArticle.getID();

                //Article IS NOT in visited
                if (!this.visited.containsKey(id))
                {
                    if (this.visited.put(id, currentArticle) != null) throw new RuntimeException("Article was already visited.");

                    HashMapArticle articleLinks = currentArticle.getLinkedArticles();
                    //Integer increasedDepth = newArticle.depth + 1;

                    //Add article links to notVisited
                    for (Map.Entry<Integer, Article> link : articleLinks.entrySet())
                    {
                        Article linkValue = link.getValue();
                        Integer linkID = link.getKey();
                        //System.out.println(linkValue.uri);

                        //Link IS NOT in visited
                        if (!this.visited.containsKey(linkID))
                        {
                            //Link IS in notVisited
                            if (this.notVisited.containsKey(linkID))
                            {
                                boolean success = this.notVisited.updateLinks(linkID, linkValue.linkedBy, linkValue.depth);

                                if (!success) throw new RuntimeException("Non-visited article not updated.");
                            }
                            //Link IS NOT in notVisited
                            else if (this.notVisited.put(linkID, linkValue) != null) throw new RuntimeException("Link not added.");
                        }
                        //Link IS in visited
                        else
                        {
                            boolean success = this.visited.updateLinks(linkID, linkValue.linkedBy, linkValue.depth);

                            if (!success) throw new RuntimeException("Visited article not updated.");
                        }
                    }

                    limit = limit - 1;
                }

                //for (Map.Entry<Integer, VisitedArticle> link : this.visited.entrySet())
                    //System.out.println(link.getValue().linkedBy.size() + " -> " + link.getValue().uri);

                //for (Article a : this.notVisited) System.out.println(a.linkedBy.size() + " > " + a.url.toString());
            }

            catch (Exception e) {
                //TODO log
                e.printStackTrace();
            }
        }
    }

    private void crawlWait (long previousTime, Integer waitFor) throws InterruptedException
    {
        long waited = System.currentTimeMillis() - previousTime;
        TimeUnit.MILLISECONDS.sleep((waited >= waitFor) ? 0 : (waitFor - waited));
    }

    private void loadCrawler()
    {
        File file1 = new File(visitedPath);
        File file2 = new File(notVisitedPath);

        //Read visited articles
        if(file1.exists() && !file1.isDirectory())
        {
            try
            {
                this.visited = HashMapArticle.readHashMap(visitedPath);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        //Read not visited articles
        if(file2.exists() && !file2.isDirectory())
        {
            try
            {
                this.notVisited = HashMapArticle.readHashMap(notVisitedPath);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void saveCrawler()
    {
        //Save visited articles
        try
        {
            this.visited.writeHashMap(visitedPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //Save not visited articles
        try
        {
            this.notVisited.writeHashMap(notVisitedPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

