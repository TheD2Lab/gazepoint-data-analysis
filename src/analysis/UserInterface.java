package analysis;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class UserInterface extends Frame {
    private static UserInterface UI;
    private FileDialog fc;
    private boolean batchAnalysis;

    private UserInterface() {
        super("The D2 Lab - Gazepoint Analysis Tool");
        setSize(800, 600);
        setLayout(new GridBagLayout());
   
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Checkbox c = new Checkbox("Batch Analysis");
        add(c);

        Button b = new Button("Select Files...");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = selectFile();
                System.out.println(file.getName());
            }
        });
        add(b);

        setResizable(false);
        setVisible(true);
    }

    public static UserInterface getInstance() {
        if (UI == null) UI = new UserInterface();
        return UI;
    }

    private File selectFile() {
        fc = new FileDialog(this, "Choose a file", FileDialog.LOAD);
        fc.setMultipleMode(true);
        fc.setDirectory("C:\\");
        fc.setVisible(true);

        File[] files = fc.getFiles();
        String filePath = fc.getDirectory() + fc.getFiles();
        return filePath != "" ? new File(filePath) : null;
    }
}
