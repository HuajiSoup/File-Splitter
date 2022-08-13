package top.huajiuniverse.FileSplitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.commons.io.*;

public class FileSplitter {
    final static int GB = 1024 * 1024 * 1024; // bytes

    public static void main(String[] args) {
        System.out.println();

        // params getting
        Options options = getOptions();
        CommandLine cmdArgs;
        CommandLineParser cmdParser = new DefaultParser();
        String fileName;
        boolean mixMode;
        int maxGSize;
        try {
            if (args.length == 0) {
                throw new ParseException("No args, to docs");
            }
            cmdArgs = cmdParser.parse(options, args);
            fileName = cmdArgs.getOptionValue("f");
            mixMode = cmdArgs.hasOption("m");
            maxGSize = cmdArgs.hasOption("s") ? Integer.parseInt(cmdArgs.getOptionValue("s")) : -1;
        } catch (Exception e) {
            System.out.println("Oops! Something is wrong with the args, please read the docs:\n");
            printDocs(options);
            return;
        }

        // start
        // mix:splitFile -> mixFile ; split: file -> splitFile
        try { // any is about files, so try
            // input & output at the same time, to avoid OutOfMemoryError
            if (mixMode) {
                mixFiles(fileName);
            } else {
                splitFile(fileName, maxGSize);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Oops! Something is wrong when operating file...");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Oh no! Failed to run this program...");
        }

        System.out.println("\nProgram finished, Goodbye!");
    }

    public static void splitFile(String fileName, int maxGSize) throws IOException {
        // read
        File file = new File(fileName);
        if (file.isDirectory() || !file.exists()) {
            System.out.printf("%s is not a file or does not exist.%n", fileName);
            return;
        }
        long fileSize = FileUtils.sizeOf(file); // bytes
        int fileGSize = (int) Math.ceil((double) fileSize / GB);
        if (fileGSize <= maxGSize) {
            System.out.printf("%s does not need to be split.%n", fileName);
            return;
        }
        FileInputStream fileFis = new FileInputStream(file);
        byte[] fileBytes; // to be every GB

        // write
        int splitNum = (int) Math.ceil((double) fileGSize / maxGSize);
        String outputPath = String.format("%s\\%s-split", file.getParent(), file.getName());
        String outputName = outputPath + "\\" + file.getName() + ".part%04d.split";
        FileUtils.forceMkdir(new File(outputPath));
        File splitFile;
        FileOutputStream splitFileFos;

        // split
        for (int i = 0; i < splitNum; i++) { // each `i` is split
            System.out.printf("Splitting part %04d...", i + 1);

            splitFile = new File(String.format(outputName, i + 1));
            FileUtils.deleteQuietly(splitFile);
            splitFileFos = new FileOutputStream(splitFile);
            for (int j = 0; j < Math.min(maxGSize, fileGSize - i * maxGSize); j++) { // j = 1GB
                fileBytes = IOUtils.toByteArray(fileFis, Math.min(GB,
                        fileSize - ((long) i * maxGSize + j) * GB)); // remember `long`!
                IOUtils.write(fileBytes, splitFileFos);
            }
            System.out.println("Done!");
        }

        System.out.printf("Successfully split the file '%s' into %d files in '%s'. %n",
                fileName, splitNum, outputPath);
    }

    public static void mixFiles(String fileName) throws IOException {
        // read
        File splitFilePath = new File(fileName);
        if (splitFilePath.isFile() || !splitFilePath.exists()) {
            System.out.printf("%s is not a directory or does not exist.%n", fileName);
            return;
        }
        List<File> splitFiles = (List<File>) FileUtils.listFiles(splitFilePath, new String[]{"split"}, false);
        int splitNum = splitFiles.size();
        if (splitNum < 2) {
            System.out.printf("%s has no files to be mixed.%n", fileName);
            return;
        }
        String filePName = splitFiles.get(0).getName();
        filePName = filePName.substring(0, filePName.length()-15);
        File splitFile;
        byte[] fileBytes;

        // write
        String outputPath = String.format("%s\\%s", fileName, filePName);
        String outputName = outputPath + "\\" + filePName;
        File mixFile = new File(outputName);
        FileUtils.deleteQuietly(mixFile);
        FileUtils.forceMkdirParent(mixFile);
        FileOutputStream fileFos = new FileOutputStream(mixFile);

        // mix
        for (int i = 0; i < splitNum; i++) {
            System.out.printf("Adding file part %04d...", i+1);

            splitFile = splitFiles.get(i);
            FileInputStream spiltFileFis = new FileInputStream(splitFile);
            long splitFileSize = FileUtils.sizeOf(splitFile);
            int splitFileGSize = (int) Math.ceil((double) splitFileSize / GB);
            for (int j = 0; j < splitFileGSize; j++) {
                fileBytes = IOUtils.toByteArray(spiltFileFis, Math.min(GB, splitFileSize - (long) j * GB));
                IOUtils.write(fileBytes, fileFos);
            }
            System.out.println("Done!");
        }

        System.out.printf("Successfully added %d files into '%s'. %n",
                splitNum,  outputName);
    }

    public static Options getOptions() {
        Options options = new Options();
        OptionGroup OpeMethods = new OptionGroup();
        options.addOption(Option.builder("f")
                .argName("file-name")
                .desc("The file to spilt when disabled `-m`;\n"+
                        "The dir with all split parts(*.part1234.split) when enabled `-m`.")
                .hasArg()
                .required()
                .build());
        OpeMethods.addOption(Option.builder("s")
                .argName("max-size")
                .desc("Enable splitting mode "+
                        "and set maximum size of each spilt file when splitting / GB (Integer).")
                .hasArg()
                .build());
        OpeMethods.addOption(Option.builder("m")
                .desc("Enable mixing mode, "+
                        "the name of the created file is from the first split file in this directory.")
                .hasArg(false)
                .build());
        OpeMethods.setRequired(true);
        options.addOptionGroup(OpeMethods);

        return  options;
    }

    public static void printDocs(Options options) {
        HelpFormatter helper = new HelpFormatter();
        System.out.println("DOCS : FileSplitter v1.0 by HuajiSoup233! \n"+
                        "This tool helps you spilt a large file into smaller ones "+
                        "and mix them all into a large one as the original.\n"+
                        "Any file should be smaller than 2147483647GB (wtf).\n");
        helper.printHelp(" ", options, true);
        System.out.println("\nFor mixing, you can also use `copy /b A+B+...` in cmd.");
    }
}
