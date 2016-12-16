package com.sandbox7.parser;

import android.graphics.Bitmap;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedReader {
    private final StringBuilder rawData;

    public FeedReader(BufferedReader reader) {
        String line;
        StringBuilder rawData = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                rawData.append(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.rawData = rawData;
    }

    public List getData() throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        Pattern patterm = Pattern.compile("<article class=\"post\"[\\w\\W]+?</article>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = patterm.matcher(rawData.toString());
        while (matcher.find()) {
            entries.add(new FeedEntry(matcher.group()));
        }

        return entries;
    }

    public static class FeedEntry implements Serializable {
        public String title;
        public String link;
        public String imageUrl;
        public transient Bitmap image;
        public String summary;
        public String createDate;

        private FeedEntry(String xmlData) {
            Pattern pattern = Pattern.compile("<h2 class=\"list-post-title\".+title=\"([\\w\\W]+?)\".+?</h2>", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(xmlData);
            if (m.find())
                title = m.group(1).replace("&amp;quot;", "\"");

            pattern = Pattern.compile("<a class=\"\\w+ \\w+\" href=\"([\\w\\W]+?)\".+?>", Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(xmlData);
            if (m.find())
                link = m.group(1);

            pattern = Pattern.compile("<img itemprop=\"image\".+?src=\"([\\w\\W]+?)\".+?/>", Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(xmlData);
            if (m.find())
                imageUrl = m.group(1);

            pattern = Pattern.compile("<div itemprop=\"description\"><p.+?>([\\w\\W]+?)</p>", Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(xmlData);
            if (m.find())
                summary = m.group(1).replace("&nbsp;", " ").replaceAll("<a.+?>([\\w\\W]+?)</a>", "$1");

            pattern = Pattern.compile("<meta itemprop=\"datePublished\" content=\"([\\w\\W]+?)\"/>", Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(xmlData);
            if (m.find()) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00").parse(m.group(1));
                    SimpleDateFormat outFormatter = new SimpleDateFormat("d MMM yyyy HH:mm");
                    createDate = outFormatter.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
