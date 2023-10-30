package Crawler;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLfile implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    private static final String regexComment = "<!--(.|\n)*?-->";

    private String path;
    private LocalDateTime modifiedDate;

    public HTMLfile (String filePath, InputStreamReader HTMLinput) throws IOException
    {
        writeFile (filePath, HTMLinput);

        this.path = filePath;
        this.modifiedDate = LocalDateTime.now();
    }

    public void writeFile (String filePath, InputStreamReader input) throws IOException
    {
        File newFile = new File(filePath);
        FileWriter fw = new FileWriter(newFile, false);

        if (input.transferTo(fw) < 1) throw new IOException("No bytes written");

        input.close();
        fw.close();
    }

    public ArrayList<String> searchHTML(String regex, boolean ignoreComment) throws IOException
    {
        ArrayList <String> matches = new ArrayList <>();
        StringBuilder stringBuilder = new StringBuilder();

        Scanner scanner = new Scanner(new File(this.path));
        while (scanner.hasNextLine()) stringBuilder.append(scanner.nextLine());
        scanner.close();

        String file = stringBuilder.toString();
        if (ignoreComment) file = file.replaceAll(regexComment, "");

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(file);

        while (matcher.find()) matches.add(matcher.group());

        return matches;
    }


}
