package com.tvanm001.ucr.edu;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Scraper {

  private static final int DELAY = 10; // 10 second delay between page queries
  private static final int NUM_PAGES = 20;  // read first 20 pages of category lists

  private static final ArrayList<String> categories = new ArrayList<>(Arrays.asList(
    "data mining",
    "databases",
    "machine learning",
    "artificial intelligence"));

  public static void crawlWikiCFP() {

    // for each category, crawl the site and create a tab separated file of the results
    categories.forEach(cat -> {

      // setup the .tsv file and CSVWriter to write out in a tab separated format to disk
      File file = new File(String.format("crawl_%s.tsv", cat.replaceAll(" ", "_").toLowerCase()));
      try (CSVWriter out = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER, '\\')) {

        // crawl the NUM_PAGES per category, send the request and store the response content
        for (int i = 1; i < NUM_PAGES + 1; i++) {

          // request the nth page
          String linkToScrape = String.format("http://www.wikicfp.com/cfp/call?conference=%s&page=%s", URLEncoder.encode(cat, "UTF-8"), i);
          Document doc = Jsoup.connect(linkToScrape).get();

          // get the conference content table
          Element table = doc.select(".contsec table table").get(2);

          // grab the rows of conference information
          // skip the rows that are center aligned (table section headers)
          Elements rows = table.select("tr:not([align='center'])");

          for(int j = 0; j < rows.size(); j+= 2){
            // each conference covers 2 'tr' elements
            // the first 'tr' element holds the acronym and the name
            // the second 'tr' element holds the when, where, deadline
            Element firstRowPart = rows.get(j);
            String conferenceAcronym = firstRowPart.select("a").get(0).text();
            String conferenceName = firstRowPart.select("td").get(1).text();
            String conferenceLocation = rows.get(j+1).select("td").get(1).text();

            // write the values out to the file
            String[] outValues = new String[]{conferenceAcronym, conferenceName, conferenceLocation};
            out.writeNext(outValues);
          }

          // IMPORTANT! Do not change the following:
          Thread.sleep(DELAY * 1000); // rate-limit the queries
        }

        // close the file
        out.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
    });

  }
}

