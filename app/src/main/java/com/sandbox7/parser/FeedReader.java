package com.sandbox7.parser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        Pattern patterm = Pattern.compile("<item>[\\w\\W]+?</item>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = patterm.matcher(rawData.toString());
        while (matcher.find()) {
            entries.add(new FeedEntry(matcher.group()));
        }

        return entries;
    }

    public static class FeedEntry implements Serializable {
        public String title;
        public String link;
        public String summary;
        public String createDate;

        private FeedEntry(String xmlData) {
            Pattern patterm = Pattern.compile("<title>([\\w\\W]+?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = patterm.matcher(xmlData);
            if (m.find())
                title = m.group(1).replace("<![CDATA[", "").replace("]]>", "").replace("&quot;", "\"");

            patterm = Pattern.compile("<link>([\\w\\W]+?)</link>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            m = patterm.matcher(xmlData);
            if (m.find())
                link = m.group(1);

            patterm = Pattern.compile("<description>([\\w\\W]+?)</description>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            m = patterm.matcher(xmlData);
            if (m.find())
                summary = m.group(1).replace("<![CDATA[", "").replace("]]>", "");

            patterm = Pattern.compile("<pubDate>([\\w\\W]+?)</pubDate>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            m = patterm.matcher(xmlData);
            if (m.find()) {
                try {
                    Date date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).parse(m.group(1));
                    SimpleDateFormat outFormatter = new SimpleDateFormat("d MMM yyyy HH:mm");
                    createDate = outFormatter.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
