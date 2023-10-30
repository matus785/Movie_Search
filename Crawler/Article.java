package Crawler;

import Crawler.Domains.Domain;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Article implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    protected URI uri;
    public Domain host;
    protected Integer depth;

    protected ArrayList<URI> linkedBy = new ArrayList<>();
    protected HTMLfile file = null;

    public Article() {}

    public Article (URI uri, Domain host, Integer sourceDepth) throws URISyntaxException
    {
        this.uri = uri;
        this.host = host;
        this.depth = sourceDepth;
    }

    public Integer getID()
    {
        return this.uri.hashCode();
    }

    public boolean isVisited()
    {
        return this.file != null;
    }

    @Override
    public int hashCode()
    {
        return this.getID();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass() && !(obj instanceof Article))
        {
            if (obj.getClass() == URI.class) return this.uri.equals(obj);
            if (obj.getClass() == URL.class)
            {
                try
                {
                    URI u = ((URL) obj).toURI();
                    return this.uri.equals(u);
                }
                catch (URISyntaxException e) {
                    return false;
                }
            }
            return false;
        }

        Article a = (Article) obj;
        return this.uri.equals(a.uri);
    }

    public void addSource(URI u, Integer sourceDepthPlus)
    {
        this.linkedBy.add(u);
        if (sourceDepthPlus < this.depth) this.depth = sourceDepthPlus;
    }

    protected URL getURL()
    {
        try
        {
            URL url = new URL(this.uri.toString());
            return url;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    //TODO prevent URL encapsulation
    protected URI createURI(String link)
    {
        try
        {
            URL url = new URL (link);

            //URL is not from source domain or is not HTML file
            if (!this.host.inDomain(url) || !host.isHTML(url)) return null;

            //System.out.println("URL = " + url.getHost() + url.getFile());
            URI uri = new URI("https://" + url.getHost() + url.getFile());

            //Article links to itself
            if (this.uri.equals(uri)) return null;

            return uri;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String getFilePath()
    {
        String fileName = this.getID() + ".html";
        return this.host.directoryPath + fileName;
    }

    public void visitArticle() throws IOException
    {
        URL url = this.getURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
        this.file = new HTMLfile(this.getFilePath(), streamReader);

        connection.disconnect();
    }

    public void revisitArticle()
    {

    }

    public HashMapArticle getLinkedArticles() throws IOException
    {
        HashMapArticle articles = new HashMapArticle();
        Integer increasedDepth = this.depth + 1;

        //Max depth reached
        if (increasedDepth > this.host.getMaxDepth() || !this.isVisited()) return articles;

        ArrayList<String> links = this.file.searchHTML(this.host.URLregex, true);

        for (String s : links)
        {
            try
            {
                URI u = this.createURI(s);
                if (u == null) continue;
                //throw new MalformedURLException("Wrong link '" + s + "' on webpage.");

                Integer id = u.hashCode();

                //Link IS in HashMap
                if (articles.containsKey(id))
                {
                    boolean success = articles.updateLinks(id, new ArrayList<URI>(List.of(u)), increasedDepth);

                    if (!success) throw new RuntimeException("Link NOT updated");
                }
                //Link IS NOT in HashMap
                else
                {
                    Article a = new Article(u, this.host, increasedDepth);
                    a.addSource(this.uri, increasedDepth);

                    if (articles.put(id,a) != null) throw new RuntimeException("Link NOT added");
                }
            }
            catch (Exception e) {
                //TODO log
                System.out.println(e);
            }
        }

        return articles;
    }

}
