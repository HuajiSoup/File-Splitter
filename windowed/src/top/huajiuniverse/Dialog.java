package top.huajiuniverse;

import javax.swing.*;
import java.awt.*;

public class Dialog {
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
