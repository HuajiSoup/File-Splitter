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

public class FileMixer extends Thread {
    public static final int GB = 1024 * 1024 * 1024; // bytes
    private final String[] fileNames;
    private final String outputFile;
    private final JProgressBar progressBar;
    private final Component parent;
    private final Component grandParent;

    public FileMixer(String[] inputFileNames,
                     String outputFile,
                     JProgressBar progressBar,
                     Component parent,
                     Component grandParent) {
        this.fileNames = inputFileNames;
        this.outputFile = outputFile;
        this.progressBar = progressBar;
        this.parent = parent;
        this.grandParent = grandParent;
    }

    @Override
    public void run() {
        try {
            // read
            int splitNum = fileNames.length; // >= 2
            progressBar.setValue(0);
            progressBar.setMaximum(splitNum);
            grandParent.setEnabled(false);
            parent.setVisible(true);

            File[] splitFiles = new File[splitNum];
            for (int i = 0; i < splitNum; i++) {
                splitFiles[i] = new File(fileNames[i]);
            }
            File splitFile;
            byte[] fileBytes;

            // write
            File mixFile = new File(outputFile);
            FileUtils.deleteQuietly(mixFile);
            FileUtils.forceMkdirParent(mixFile);
            FileOutputStream fileFos = new FileOutputStream(mixFile);

            // mix
            for (int i = 0; i < splitNum; i++) {
                splitFile = splitFiles[i];
                FileInputStream spiltFileFis = new FileInputStream(splitFile);
                long splitFileSize = FileUtils.sizeOf(splitFile);
                int splitFileGSize = (int) Math.ceil((double) splitFileSize / GB);
                for (int j = 0; j < splitFileGSize; j++) {
                    fileBytes = IOUtils.toByteArray(spiltFileFis, Math.min(GB, splitFileSize - (long) j * GB));
                    IOUtils.write(fileBytes, fileFos);
                }

                IOUtils.close(spiltFileFis);
                progressBar.setValue(progressBar.getValue() + 1);
            }

            IOUtils.close(fileFos);
        } catch (IOException ex) { // repeat, but okay...
            Dialog.showError(parent, "Something is wrong with the files.\n"+ex.getMessage());
        } catch (Exception ex) {
            Dialog.showError(parent, "Something is wrong with the program.\n"+ex.getMessage());
        } finally {
            parent.setVisible(false);
            grandParent.setEnabled(true);
        }
    }
}
