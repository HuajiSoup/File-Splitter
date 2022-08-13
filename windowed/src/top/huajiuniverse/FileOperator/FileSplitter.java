package top.huajiuniverse.FileOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import top.huajiuniverse.Dialog;

public class FileSplitter extends Thread {
    public static final int GB = 1024 * 1024 * 1024; // bytes
    private final String fileName;
    private final String outputPath;
    private final int maxGSize;
    private final JProgressBar progressBar;
    private final Component parent;
    private final Component grandParent;

    public FileSplitter(String inputFileName,
                        String outputPath,
                        int maxGSize,
                        JProgressBar progressBar,
                        Component parent,
                        Component grandParent) {
        this.fileName = inputFileName;
        this.outputPath = outputPath;
        this.maxGSize = maxGSize;
        this.progressBar = progressBar;
        this.parent = parent;
        this.grandParent = grandParent;
    }

    @Override
    public void run() {
        try {
            File file = new File(fileName);
            long fileSize = FileUtils.sizeOf(file); // bytes
            int fileGSize = (int) Math.ceil((double) fileSize / GB);
            if (fileGSize <= maxGSize) {
                Dialog.showWarning(parent, String.format("This file does not > %dGB", maxGSize));
                return;
            }
            progressBar.setValue(0);
            progressBar.setMaximum(fileGSize);
            grandParent.setEnabled(false);
            parent.setVisible(true);

            FileInputStream fileFis = new FileInputStream(file);
            byte[] fileBytes; // to be every GB

            // write
            int splitNum = (int) Math.ceil((double) fileGSize / maxGSize);
            String outputName = outputPath + "\\" + file.getName() + ".part%04d.split";
            FileUtils.forceMkdir(new File(outputPath));
            File splitFile;
            FileOutputStream splitFileFos;

            // split
            for (int i = 0; i < splitNum; i++) { // each `i` is split
                splitFile = new File(String.format(outputName, i + 1));
                FileUtils.deleteQuietly(splitFile);
                splitFileFos = new FileOutputStream(splitFile);
                for (int j = 0; j < Math.min(maxGSize, fileGSize - i * maxGSize); j++) { // j = 1GB
                    fileBytes = IOUtils.toByteArray(fileFis, Math.min(GB,
                            fileSize - ((long) i * maxGSize + j) * GB)); // remember `long`!
                    IOUtils.write(fileBytes, splitFileFos);
                    progressBar.setValue(progressBar.getValue() + 1);
                }

                IOUtils.close(splitFileFos);
            }

            IOUtils.close(fileFis);
        } catch (IOException ex) {
            Dialog.showError(parent, "Something is wrong with the files.\n"+ex.getMessage());
        } catch (Exception ex) {
            Dialog.showError(parent, "Something is wrong with the program.\n"+ex.getMessage());
        } finally {
            parent.setVisible(false);
            grandParent.setEnabled(true);
        }
    }
}
