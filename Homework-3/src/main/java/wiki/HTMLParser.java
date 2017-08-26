package wiki;

import java.io.*;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLParser {

    public static void main(String args[]) throws IOException {

        if (args.length != 1) {
            System.out.println("Input bz2 file required on command line.");
            System.exit(1);
        }

        BufferedReader reader = null;
        File inputFile = new File(args[0]);
        if (!inputFile.exists() || inputFile.isDirectory() || !inputFile.getName().endsWith(".bz2")) {
            System.out.println("Input File does not exist or not bz2 file: " + args[0]);
            System.exit(1);
        }


        BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(inputFile));
        reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter out = new PrintWriter(new FileWriter("output.txt"));
        String line;

        while ((line = reader.readLine()) != null) {
            Document html = Jsoup.parse(line);
            out.print(html);
        }
        out.close();

    }
}





