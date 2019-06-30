package uk.ac.cam.cl.ap949.exercises;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;



public class Exercise3 {
    static final Path dataDirectory = Paths.get("data/large_dataset");
    Map<String, Integer> freq;




    public Exercise3() throws IOException {
        freq = findFrequency();
    }

    public Map<String, Integer> findFrequency() throws IOException {
        Map<String, Integer> frequency = new TreeMap<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
            for(Path filePath: files) {
                List<String> words = Tokenizer.tokenize(filePath);
                for(String word: words) {
                    if(frequency.containsKey(word)) {
                        frequency.put(word, 1);
                    } else {
                        frequency.put(word, frequency.get(word+1));
                    }
                }
            }
        }

        return frequency;
    }


    public static void main(String[] args) throws IOException{
        Exercise3 ex3 = new Exercise3();

    }
}
