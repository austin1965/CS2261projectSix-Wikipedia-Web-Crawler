import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    final static int PAGE_CRAWL_LIMIT = 10;
    final static String STARTING_URL = "https://en.wikipedia.org/wiki/Thalassodromeus";

    public static void main(String[] args) throws IOException, InterruptedException {

        String[] stringList = getWordList(STARTING_URL);
        ArrayList<String> pageUrls = pullPageLinks(STARTING_URL);
        HashMap<String, Integer> dictionary = makeWordDictionary(stringList);
        int counter = 1;

        while (counter < PAGE_CRAWL_LIMIT) {
            dictionary = retrieveDictionary(pageUrls, dictionary);
            System.out.println("Pages crawled: " + counter);
            ++counter;
            Thread.sleep(50L);
        }

        cleanDictionary(dictionary);
        for (Map.Entry entry: dictionary.entrySet()) {
            System.out.println(entry);
        }
    }

    public static String[] getWordList(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
    }

    public static HashMap<String, Integer> makeWordDictionary(String[] stringList) {
        HashMap<String, Integer> dictionary = new HashMap<>();

        for(String word: stringList) {
            if (dictionary.containsKey(word)) {
                int value = dictionary.get(word) + 1;
                dictionary.put(word, value);
            }
            else {
                dictionary.put(word, 1);
            }
        }
        return dictionary;
    }

    public static HashMap<String, Integer> retrieveDictionary(ArrayList<String> pageUrls,
                                                              HashMap<String, Integer> dictionary) throws IOException {

        Random random = new Random();
        int randomNum = random.nextInt(pageUrls.size()-1);
        String newPage = pageUrls.get(randomNum);
        String[] newWordList = getWordList(newPage);
        ArrayList<String> newUrlList = pullPageLinks(newPage);
        HashMap<String, Integer> newDict = makeWordDictionary(newWordList);
        dictionary = dictionaryMerge(dictionary, newDict);

        return dictionary;
    }

    public static ArrayList<String> pullPageLinks(String url) throws IOException {
        Elements elements = Jsoup.connect(url).get().getElementById("content").getElementsByTag("a");
        ArrayList<String> urlList= new ArrayList<String>();

        for (Element link : elements) {
            if(link.attr("href").contains("wiki")
                    && !link.attr("href").contains("http")
                    && !link.attr("href").contains("template:")
                    && !link.attr("href").contains("Template:")
                    && !link.attr("href").contains("Category:")
                    && !link.attr("href").contains("category:")
                    && !link.attr("href").contains("File:")
                    && !link.attr("href").contains("file:")
                    && !link.attr("href").contains("Special:")
                    && !link.attr("href").contains("special:")
                    && !link.attr("href").contains("Help:")
                    && !link.attr("href").contains("help:")
                    && !link.attr("href").contains("Portal:")
                    && !link.attr("href").contains("portal:")
                    && !link.attr("href").contains("Wikipedia:")
                    && !link.attr("href").contains("#")
                    && !link.attr("href").contains("(")) {
                urlList.add("https://en.wikipedia.org" + link.attr("href"));
            }
        }

        return urlList;
    }

    public static HashMap<String, Integer> dictionaryMerge(
            HashMap<String, Integer> dictionaryOne,
            HashMap<String, Integer>dictionaryTwo) {

        HashMap<String, Integer> dictionaryThree = new HashMap<String, Integer>(dictionaryOne);
        for(Map.Entry<String, Integer> entry: dictionaryTwo.entrySet()) {
            if (dictionaryThree.containsKey(entry.getKey())) {
                int tempVal = dictionaryThree.get(entry.getKey()) + entry.getValue();
                dictionaryThree.put(entry.getKey(), tempVal);
            }
            else {
                dictionaryThree.put(entry.getKey(), entry.getValue());
            }
        }

        return dictionaryThree;
    }

    public static void cleanDictionary(HashMap<String, Integer> dictionary) {
        dictionary.entrySet().removeIf(stringIntegerEntry -> stringIntegerEntry.getKey().contains("http"));
    }

}
