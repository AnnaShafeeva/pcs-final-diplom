import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> searchResults = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] listOfPdfs = pdfsDir.listFiles();
        if (listOfPdfs != null) {
            for (File pdfFile : listOfPdfs) {
                PdfDocument doc = new PdfDocument(new PdfReader(pdfFile));
                for (int page = 1; page <= doc.getNumberOfPages(); page++) {
                    PdfPage currentPage = doc.getPage(page);
                    String text = PdfTextExtractor.getTextFromPage(currentPage);
                    String[] words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>();
                    for (String word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        word = word.toLowerCase();
                        freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                    }
                    for (String word : freqs.keySet()) {
                        List<PageEntry> pageEntries = new ArrayList<>();
                        if (!searchResults.containsKey(word)) {
                            pageEntries.add(new PageEntry(pdfFile.getName(), page, freqs.get(word)));
                            searchResults.put(word, pageEntries);
                        } else {
                            List<PageEntry> value = searchResults.get(word); // word, <devops.pdf, 1, 3> + <devops.pdf, 2, 2>
                            value.add(new PageEntry(pdfFile.getName(), page, freqs.get(word)));
                            value.sort(PageEntry::compareTo);
                            searchResults.put(word, value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        String lowerCaseWord = word.toLowerCase();
        if (searchResults.containsKey(lowerCaseWord)) {
            return searchResults.get(lowerCaseWord);
        }
        return Collections.emptyList();
    }
}