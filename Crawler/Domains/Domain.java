package Crawler.Domains;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;

public class Domain implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String URLregex = "(?<=href=[\"'])[^\"' ]+(?=[\"'])";
    //public static final Integer maxDepth = 25;
    //public static final Integer delay = 25;

    public String directoryPath;
    public String name;


    public Domain(String host, String crawlerDirectory) throws MalformedURLException
    {
        URL url = new URL(host);
        this.name = getDomainName(url);
        this.directoryPath = crawlerDirectory + "data\\" + this.name + "\\";

        File dir = new File(this.directoryPath);
        if (!dir.exists()) dir.mkdirs();
    }

    public boolean inDomain(URL url)
    {
        return url.getHost().contains(this.name);
    }

    public static boolean isHTML(URL url)
    {
        String str = url.getPath();
        Integer index = str.lastIndexOf('.');

        if (index == -1) return true;
        String fileType = str.substring(index + 1);

        return fileType.contains("org") || fileType.contains("com") || fileType.contains("html");
    }

    public static String getDomainName(URL url)
    {
        String s = url.getHost();

        return s.substring(s.indexOf(".") + 1, s.lastIndexOf("."));
    }

    public static Integer getDelay()
    {
        return 1500;
    }

    public static Integer getMaxDepth()
    {
        return 25;
    }

    public static String getMovieName(URL url)
    {
        String movie = url.getFile().replaceAll("/", "");

        movie = movie.replaceAll("-","_");

        if (movie.equals("")) return "domain";
        return movie;
    }

}
