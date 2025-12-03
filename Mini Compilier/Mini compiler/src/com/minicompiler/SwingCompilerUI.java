package com.minicompiler;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;

public class SwingCompilerUI {
    private static final JTextArea codeArea = new JTextArea();
    private static final JTextArea resultArea = new JTextArea();
    private static final JButton lexicalBtn = createCurvyButton("Lexical<br>Analysis", new Color(220, 20, 60));
    private static final JButton syntaxBtn = createCurvyButton("Syntax<br>Analysis", new Color(255, 215, 0));
    private static final JButton semanticBtn = createCurvyButton("Semantic<br>Analysis", new Color(0, 230, 118));
    private static ScriptEngine jsEngine;

    public static void main(String[] args) {
        jsEngine = new ScriptEngineManager().getEngineByName("graal.js");
        if (jsEngine == null) jsEngine = new ScriptEngineManager().getEngineByName("nashorn");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("TextArea.background", Color.BLACK);
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
        UIManager.put("ScrollBar.background", Color.DARK_GRAY);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        SwingUtilities.invokeLater(SwingCompilerUI::createGUI);
    }

    // Custom glowing light-blue border
    private static Border createGlowBorder(String title) {
        Color glowColor = new Color(100, 200, 255); // Soft light blue
        Border line = BorderFactory.createLineBorder(glowColor, 2);
        Border titleBorder = BorderFactory.createTitledBorder(
                line, title, 0, 0, new Font("Arial", Font.BOLD, 14), glowColor);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                titleBorder
        );
    }

    private static JButton createCurvyButton(String text, Color bg) {
        JButton btn = new JButton("<html><center><b>" + text + "</b></center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {}
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.BLACK);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 70));
        btn.setMaximumSize(new Dimension(180, 70));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setPreferredSize(new Dimension(195, 82));
                btn.setBackground(bg.brighter());
                btn.revalidate();
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setPreferredSize(new Dimension(180, 70));
                btn.setBackground(bg);
                btn.revalidate();
                btn.repaint();
            }
        });

        return btn;
    }

    private static void createGUI() {
        JFrame f = new JFrame("MiniCompiler - Dark Theme");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 750);
        f.getContentPane().setBackground(Color.BLACK);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 12, 18));
        buttons.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buttons.setBackground(Color.BLACK);

        JButton openBtn = createCurvyButton("Open<br>File", new Color(32, 190, 180));
        JButton clearBtn = createCurvyButton("Clear", new Color(237, 42, 133));

        lexicalBtn.setEnabled(false);
        syntaxBtn.setEnabled(false);
        semanticBtn.setEnabled(false);

        buttons.add(openBtn);
        buttons.add(lexicalBtn);
        buttons.add(syntaxBtn);
        buttons.add(semanticBtn);
        buttons.add(clearBtn);

        // Result Output Panel - Black background + light blue glow border
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        resultArea.setBackground(Color.BLACK);
        resultArea.setForeground(Color.CYAN);
        resultArea.setBorder(createGlowBorder(" Result Output "));

        // Source Code Panel - Black background + light blue glow border
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        codeArea.setBackground(Color.BLACK);
        codeArea.setForeground(Color.WHITE);
        codeArea.setBorder(createGlowBorder(" Source Code "));
        codeArea.setCaretColor(Color.WHITE);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(resultArea), new JScrollPane(codeArea));
        rightSplit.setDividerLocation(320);
        rightSplit.setBackground(Color.BLACK);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttons, rightSplit);
        mainSplit.setDividerLocation(240);
        mainSplit.setBackground(Color.BLACK);

        f.add(mainSplit);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        openBtn.addActionListener(e -> openFile(f));
        lexicalBtn.addActionListener(e -> runJS("src/com/minicompiler/analyzer/LexicalAnalyzer.js", lexicalBtn, syntaxBtn));
        syntaxBtn.addActionListener(e -> runJS("src/com/minicompiler/analyzer/SyntaxAnalyzer.js", syntaxBtn, semanticBtn));
        semanticBtn.addActionListener(e -> runJS("src/com/minicompiler/analyzer/SemanticAnalyzer.js", semanticBtn, null));
        clearBtn.addActionListener(e -> {
            codeArea.setText("");
            resultArea.setText("");
            resetButtons();
        });

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            private void update() {
                lexicalBtn.setEnabled(!codeArea.getText().trim().isEmpty());
            }
        });
    }

    private static void openFile(JFrame frame) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                codeArea.setText(Files.readString(fc.getSelectedFile().toPath()));
                resultArea.setText("File opened: " + fc.getSelectedFile().getName() + "\n\n");
                resetButtons();
            } catch (Exception ex) {
                resultArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }
    }

    private static void runJS(String jsFilePath, JButton currentBtn, JButton nextBtn) {
        try {
            String script = Files.readString(new File(jsFilePath).toPath());
            jsEngine.put("sourceCode", codeArea.getText());
            Object output = jsEngine.eval(script);

            resultArea.append("=== " + new File(jsFilePath).getName() + " ===\n");
            resultArea.append((output == null ? "No output\n" : output.toString()) + "\n\n");

            if (currentBtn != null) currentBtn.setEnabled(false);
            if (nextBtn != null) nextBtn.setEnabled(true);
        } catch (Exception ex) {
            resultArea.append("ERROR in " + jsFilePath + ":\n" + ex.getMessage() + "\n\n");
            resetButtons();
        }
    }

    private static void resetButtons() {
        boolean hasText = !codeArea.getText().trim().isEmpty();
        lexicalBtn.setEnabled(hasText);
        syntaxBtn.setEnabled(false);
        semanticBtn.setEnabled(false);
    }
}
