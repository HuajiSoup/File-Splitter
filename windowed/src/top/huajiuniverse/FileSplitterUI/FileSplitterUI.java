package top.huajiuniverse.FileSplitterUI;

import top.huajiuniverse.FileOperator.FileMixer;
import top.huajiuniverse.FileOperator.FileSplitter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import top.huajiuniverse.Dialog;

public class FileSplitterUI {
    final static int SPLIT_MODE = 0;
    final static int SINGLE_FILE = 0;
    final static int MULTI_FILES = 1;

    public static void main(String[] args) {
        // what a bad layout Swing is using!!
        // I prefer null layout!!
        // Numbers never lie, but Swing does :(
        FileSplitterUI tf = new FileSplitterUI(); // This File

        // init init
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error!");
        }
        Font font = new Font(null, Font.PLAIN, 14);
        Image icon = Toolkit.getDefaultToolkit().createImage(tf.url("/res/file_splitter.png"));

        // init main frame
        JFrame mainFrame = new JFrame("File Splitter");
        mainFrame.setSize(600, 456);
        mainFrame.setResizable(false);
        mainFrame.setIconImage(icon);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(null);
        JTextArea tempArea = new JTextArea(); // save some temp data to solve a strange problem
        tempArea.setVisible(false);

        // init run frame
        JFrame runFrame = new JFrame("Operating files...");
        runFrame.setSize(250, 130);
        runFrame.setResizable(false);
        runFrame.setIconImage(icon);
        runFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        runFrame.setVisible(false);

        JPanel runPanel = new JPanel(null);
        JLabel runLabel = new JLabel("Operating... Sit and relax!");
        JProgressBar runBar = new JProgressBar();
        runLabel.setBounds(20, 10, 210, 30);
        runBar.setBounds(20, 50, 210, 22);
        runLabel.setFont(font);
        addComps(runPanel, runLabel, runBar);

        runFrame.setContentPane(runPanel);

        // Ope1 : init
        String[] modeList = new String[]{"Splitting", "Mixing"};
        ImageIcon[] arrowImages = new ImageIcon[]{new ImageIcon(tf.url("/res/arrow_down.png")),
                new ImageIcon("./res/arrow_up.png")};
        NumberFormat intFormatter = NumberFormat.getIntegerInstance();

        JLabel opeLabel = new JLabel("Mode = ");
        JComboBox<String> opeCombo = new JComboBox<>(modeList);
        JLabel maxSizeLabel = new JLabel("Max size = ");
        JLabel unitGbLabel = new JLabel("GB (integer)");
        JFormattedTextField maxSizeField = new JFormattedTextField(intFormatter);
        maxSizeField.setValue(4);
        JLabel direLabel = new JLabel();
        direLabel.setIcon(arrowImages[0]);
        JButton startBtn = new JButton("START");
        startBtn.setIcon(new ImageIcon(tf.url("/res/start.png")));

        // mix file box
        JTextField mixField = new JTextField();
        JButton mixBrowseBtn = new JButton("Browse...");
        mixBrowseBtn.addActionListener(e -> {
            if (opeCombo.getSelectedIndex() == SPLIT_MODE) { // 100% exists
                chooseFile(mainPanel, SINGLE_FILE, tempArea);
            } else {
                saveFile(mainPanel, JFileChooser.FILES_ONLY, tempArea);
            }
            String choseFilePath = tempArea.getText();
            tempArea.setText("");
            mixField.setText(choseFilePath);
        });

        mixField.setBounds(30, 60, 430, 30);
        mixBrowseBtn.setBounds(460, 60, 90, 30);

        // split files box
        DefaultListModel<String> fileListModel = new DefaultListModel<>(); // edit this, like tree
        JList<String> fileList = new JList<>(fileListModel);
        JScrollPane scrollPanel = new JScrollPane(fileList);

        Box SplitBtnBox = Box.createVerticalBox();
        JButton addBtn = new JButton(new ImageIcon(tf.url("/res/button_add.png")));
        JButton delBtn = new JButton(new ImageIcon(tf.url("/res/button_del.png")));
        JButton moveUpBtn = new JButton(new ImageIcon(tf.url("/res/button_move_up.png")));
        JButton moveDownBtn = new JButton(new ImageIcon(tf.url("/res/button_move_down.png")));
        JButton sortBtn = new JButton(new ImageIcon(tf.url("/res/button_sort.png")));
        addBtn.setToolTipText("Add files");
        delBtn.setToolTipText("Delete files");
        moveUpBtn.setToolTipText("Move files up");
        moveDownBtn.setToolTipText("Move files down");
        sortBtn.setToolTipText("Auto sorting all files by filename");

        addBtn.addActionListener(e -> {
            if (opeCombo.getSelectedIndex() == SPLIT_MODE) {
                saveFile(mainPanel, JFileChooser.DIRECTORIES_ONLY, tempArea);
                String choseFilePath = tempArea.getText();
                if (!choseFilePath.equals("")) {
                    fileListModel.add(fileListModel.size(), choseFilePath);
                    addBtn.setEnabled(false); // when split, output can only be 1 directory
                }
            } else {
                chooseFile(mainPanel, MULTI_FILES, tempArea);
                ArrayList<String> choseFilesPath = new ArrayList<>(Arrays.asList(tempArea.getText().split("\n")));
                if (choseFilesPath.size() > 0) {
                    fileListModel.addAll(choseFilesPath);
                }
            }
            tempArea.setText("");
        });

        delBtn.addActionListener(e -> {
            int[] selectedIndices = fileList.getSelectedIndices();
            for (int i = 0; i < selectedIndices.length; i++) {
                fileListModel.remove(selectedIndices[i] - i);
            }

            if ((selectedIndices.length > 0) && (opeCombo.getSelectedIndex() == SPLIT_MODE)) {
                addBtn.setEnabled(true);
            }
        });

        moveUpBtn.addActionListener(e -> {
            int[] selectedIndices = fileList.getSelectedIndices();
            int[] newSelectedIndices = Arrays.copyOf(selectedIndices, selectedIndices.length);
            int n;
            int movedLimit = 0;
            int movedIndex;
            String tempStr;
            while ((movedLimit < selectedIndices.length - 1) &&
                    (movedLimit == selectedIndices[movedLimit])) { // limit elements at the top
                movedLimit++;
            }

            for (int i = movedLimit; i < selectedIndices.length; i++) {
                n = selectedIndices[i];
                movedIndex = Math.max(n - 1, movedLimit);
                tempStr = fileListModel.getElementAt(movedIndex);
                fileListModel.setElementAt(fileListModel.getElementAt(n), movedIndex);
                fileListModel.setElementAt(tempStr, n);
                newSelectedIndices[i] = movedIndex;
            }
            fileList.setSelectedIndices(newSelectedIndices);
        });

        moveDownBtn.addActionListener(e -> {
            int[] selectedIndices = fileList.getSelectedIndices();
            int[] newSelectedIndices = Arrays.copyOf(selectedIndices, selectedIndices.length);
            int listLength = fileListModel.size();
            int n;
            int movedLimit = selectedIndices.length - 1;
            int movedIndex;
            String tempStr;
            while ((movedLimit >= 0) &&
                    (selectedIndices.length-movedLimit == listLength-selectedIndices[movedLimit])) {
                movedLimit--;
            }

            for (int i = movedLimit; i >= 0; i--) {
                n = selectedIndices[i];
                movedIndex = Math.min(n + 1, listLength - 1);
                tempStr = fileListModel.getElementAt(movedIndex);
                fileListModel.setElementAt(fileListModel.getElementAt(n), movedIndex);
                fileListModel.setElementAt(tempStr, n);
                newSelectedIndices[i] = movedIndex;
            }
            fileList.setSelectedIndices(newSelectedIndices);
        });

        sortBtn.addActionListener(e -> {
            fileList.setSelectedIndices(new int[]{});
            String[] newFileList = new String[fileListModel.size()];
            for (int i = 0; i < newFileList.length; i++) {
                newFileList[i] = fileListModel.getElementAt(i);
            }
            Arrays.sort(newFileList);
            for (int i = 0; i < newFileList.length; i++) {
                fileListModel.setElementAt(newFileList[i], i);
            }
        });

        addComps(SplitBtnBox, addBtn, delBtn, moveUpBtn, moveDownBtn, sortBtn);
        scrollPanel.setBounds(30, 122, 485, 25);
        SplitBtnBox.setBounds(518, 122, 32, 286);


        // Ope2 : functions and pack
        opeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                direLabel.setIcon(arrowImages[opeCombo.getSelectedIndex()]);
                mixField.setText("");
                fileListModel.clear();
                addBtn.setEnabled(true);
                if (opeCombo.getSelectedIndex() == SPLIT_MODE) {
                    scrollPanel.setBounds(30, 122, 485, 25);
                    fileList.setBounds(30, 122, 485, 25);
                    maxSizeLabel.setEnabled(true);
                    unitGbLabel.setEnabled(true);
                    maxSizeField.setEnabled(true);
                } else {
                    scrollPanel.setBounds(30, 122, 485, 286);
                    fileList.setBounds(30, 122, 485, 286);
                    maxSizeLabel.setEnabled(false);
                    unitGbLabel.setEnabled(false);
                    maxSizeField.setEnabled(false);
                }
            }
        });

        startBtn.addActionListener(e -> {
            if (opeCombo.getSelectedIndex() == SPLIT_MODE) {
                if (mixField.getText().equals("")) {
                    Dialog.showWarning(mainPanel, "Please specify the directory of original file.");
                    return;
                }
                if (fileListModel.size() < 1) {
                    Dialog.showWarning(mainPanel, "Please specify the directory of split file.");
                    return;
                }

                FileSplitter fileSplitter = new FileSplitter(
                        mixField.getText(),
                        fileListModel.elementAt(0),
                        ((Number) maxSizeField.getValue()).intValue(),
                        runBar,
                        runFrame,
                        mainFrame);
                fileSplitter.start();
            } else {
                if (fileListModel.size() < 2) {
                    Dialog.showWarning(mainPanel, "There should be at least 2 files in split file list.");
                    return;
                }

                String[] fileNameList = new String[fileListModel.size()];
                for (int i = 0; i < fileNameList.length; i++) {
                    fileNameList[i] = fileListModel.get(i);
                }
                FileMixer fileMixer = new FileMixer(
                        fileNameList,
                        mixField.getText(),
                        runBar,
                        runFrame,
                        mainFrame);
                fileMixer.start();
            }
        });

        opeLabel.setBounds(30, 10, 60, 30);
        opeCombo.setBounds(85, 10, 100, 30);
        maxSizeLabel.setBounds(210, 10, 70, 30);
        maxSizeField.setBounds(280, 10, 30, 30);
        unitGbLabel.setBounds(310, 10, 85, 30);
        direLabel.setBounds(284, 90, 32, 32);
        startBtn.setBounds(450, 10, 100, 30);

        // pack
        addComps(mainPanel, mixField, mixBrowseBtn, scrollPanel, SplitBtnBox,
                opeLabel, opeCombo, maxSizeLabel, maxSizeField, unitGbLabel, direLabel, startBtn);
        setFont(font, mixField, mixBrowseBtn, fileList, SplitBtnBox,
                opeLabel, opeCombo, maxSizeLabel, maxSizeField, unitGbLabel, direLabel, startBtn);
        mainFrame.setContentPane(mainPanel);
        mainFrame.setVisible(true);
    }

    private static void chooseFile(Component parent, int numMode, JTextArea outputArea) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(numMode == MULTI_FILES); // MIX_MODE or MULTI_FILES

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (numMode == 1) { // multi
                File[] choseFiles = fileChooser.getSelectedFiles();
                for (File choseFile : choseFiles) {
                    outputArea.append(choseFile.getAbsolutePath() + "\n");
                }
            } else { // single
                outputArea.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else {
            outputArea.setText("");
        }
    }

    private static void saveFile(Component parent, int fileSelectionMode, JTextArea outputArea) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setFileSelectionMode(fileSelectionMode);
        fileChooser.setMultiSelectionEnabled(false); // always a single file or dir

        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputArea.setText(fileChooser.getSelectedFile().getAbsolutePath());
        } else {
            outputArea.setText("");
        }
    }

    private static void addComps(JComponent parent, Component... children) {
        for (Component child : children) {
            parent.add(child);
        }
    }

    private static void setFont(Font font, Component... comps) {
        for (Component comp : comps) {
            comp.setFont(font);
        }
    }

    private URL url(String srcFilePath) {
        return this.getClass().getResource(srcFilePath);
    }
}
