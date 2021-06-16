package com.example.englishwords.learning;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Test {
    private static final String PATHNAME = "Vocabulary list_Straightforward_Exam B2.";
    private static final List<Map<String, String>> DICTIONARY = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException {
        getData();
        play();
    }

    private static void play() throws IOException {
        int rand = 0;
        int attempts = 0;
        int points = 0;
        int unit = 0;
        Map<String, String> answers = new LinkedHashMap();
        System.out.println("Choose unit:");
        unit = new Scanner(System.in).nextInt();
        Object[] dict = DICTIONARY.get(unit).keySet().toArray();
        while (true) {
            rand = RANDOM.nextInt(dict.length);
            System.out.println(DICTIONARY.get(unit).values().toArray()[rand]);
            String answer = new Scanner(System.in).nextLine();
            if (answer.trim().equalsIgnoreCase("exit")) {
                String info = "Your score:\t" + points + " / " + attempts + "\nAll incorrect answers:\n";
                System.out.println(info);
                answers.forEach((key, value) -> System.out.println(key + "\t-\t" + value));
                writeToFile(info, answers);
                return;
            }
            if (answer.trim().equalsIgnoreCase("change")) {
                System.out.println("Change unit:");
                unit = new Scanner(System.in).nextInt();
                dict = DICTIONARY.get(unit).keySet().toArray();
            } else {
                attempts++;
                System.out.println(dict[rand]);
                if (answer.trim().equalsIgnoreCase(dict[rand].toString().trim()) ||
                        answer.replaceAll("\\s+", "").equalsIgnoreCase(dict[rand].toString().replaceAll("\\s+", ""))) {
                    points++;
                    System.out.println("CORRECT\t+1\t" + points);
                } else {
                    answers.put(DICTIONARY.get(unit).values().toArray()[rand].toString(), dict[rand].toString());
                    System.out.println("INCORRECT\t0\t" + points);
                }
            }
        }
    }

    private static void getData() throws IOException {
        if (!new File(PATHNAME + "txt").isFile()) {
            generateTxtFromPDF();
        }
        try (Stream<String> lines = Files.lines(Path.of(PATHNAME + "txt"), StandardCharsets.UTF_8)) {
            splitString(lines);
        }
    }

    private static void splitString(Stream<String> lines) {
        Iterable<String> iterable = lines::iterator;
        int tempIndex = 0;
        Map<String, String> tempMap = new HashMap<>();
        String[] parts = new String[2];
        for (String s : iterable) {
            if (s.matches(" \\d ")) {
                tempIndex = Integer.parseInt(s.trim());
                tempMap = new HashMap<>();
                DICTIONARY.add(tempMap);
            }
            if (Character.isDigit(s.charAt(0))) {
                if (s.contains("villain złoczyńca")) {
                    parts[0] = "villain";
                    parts[1] = "złoczyńca";
                } else if (s.contains("to be familiar with sth")) {
                    parts[0] = "to be familiar with sth / his readers are familiar with basic concepts/ The house looked  familiar";
                    parts[1] = "zaznajomiony / znajomy";
                } else {
                    String delimiter = "";
                    String[] elements = Arrays.stream(s.split("\\d+. "))
                            .map(String::trim)
                            .toArray(String[]::new);
                    parts = String.join(delimiter, elements)
                            .split("\\–");
                    if (parts.length == 1) {
                        parts = String.join(delimiter, elements)
                                .split("\\ - ");
                    }
                    if (parts.length == 1) {
                        parts = String.join(delimiter, elements)
                                .split("\\- ");
                    }
                    if (parts.length == 1) {
                        parts = String.join(delimiter, elements)
                                .split("\\- ");
                    }
                }
                try {
                    tempMap.put(parts[0].trim(), parts[1].trim());
                    DICTIONARY.add(tempIndex, tempMap);
                } catch (IndexOutOfBoundsException ignored) {
                    System.out.println(s);
                }
            }
        }
    }

    private static void generateTxtFromPDF() throws IOException {
        PDFParser parser = new PDFParser(new RandomAccessFile(new File(PATHNAME + "pdf"), "r"));
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDDocument pdDoc = new PDDocument(cosDoc);
        String parsedText = new PDFTextStripper().getText(pdDoc);

        if (cosDoc != null) {
            cosDoc.close();
        }
        pdDoc.close();

        PrintWriter pw = new PrintWriter(PATHNAME + "txt");
        pw.print(parsedText);
        pw.close();
    }

    private static void writeToFile(String info, Map<String, String> answers) {
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(new File("incorrect_answers.txt")))) {
            bf.write(info);
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                bf.write(entry.getKey() + "\t:\t" + entry.getValue());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
