import Crawler.WebCrawler;

public class Main
{
    private static final String [] domains = new String [] {"https://www.geeksforgeeks.org/"};

    public static void main(String[] args)
    {
        WebCrawler crawler = new WebCrawler(domains);

        crawler.crawl(5);
        crawler.saveCrawler();

        System.out.println("END of crawl");
    }

}