package com.github.brickwall2900;

import com.github.brickwall2900.dialogs.DiaryLoadDialog;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DiaryMarkdownToHTML {
    private static class CacheRecord {
        final String content;
        long time;

        private CacheRecord(String content) {
            this.content = content;
        }
    }

    protected static final Map<Integer, CacheRecord> MD_TO_HTML_CACHE = new HashMap<>();

    private static final String LOAD_MESSAGE = "Please wait...";

    public static String getHTMLFromMarkdown(String md) {
        int givenHash = md.hashCode();
        CacheRecord record = MD_TO_HTML_CACHE.get(givenHash);
        String toReturn;
        if (record != null) {
            record.time = System.currentTimeMillis();
            toReturn = record.content;
        } else {
            DiaryEntryLoader entryLoader = new DiaryEntryLoader(md);
            entryLoader.addPropertyChangeListener(e -> {
                DiaryFrame frame = Main.INSTANCE.frame;
                if (frame != null) {
                    DiaryLoadDialog load = frame.loadDialog; // ???
                    switch (e.getPropertyName()) {
                        case "progress" -> load.setProgress((Integer) e.getNewValue());
                        case "name" -> load.setTaskName((String) e.getNewValue());
                    }
                }
            });
            entryLoader.execute();
            toReturn = LOAD_MESSAGE;
        }
        MD_TO_HTML_CACHE.entrySet().removeIf(entry -> (System.currentTimeMillis() - entry.getValue().time) >= entry.getValue().content.length() * 25L);
        return toReturn;
    }

    public static String wrapHTMLIntoActualDocument(String html) {
        return """
                <html>
                	<body>
                """
                + html +
                """
                	</body>
                </html>
                """;
    }

    public static final String DIARY_HELP = """
            Looks like you've just stumbled upon this application without knowing how to use it.
            
            Do you know what this application is in the first place?
            
            This diary uses Markdown as input to render an entry onto the screen.
            
            Use Ctrl+N to create a new entry, Ctrl+E to edit an entry, Ctrl+F to jump to an entry, Left and Right arrows to go back and forth entries.
            
            ***(WARNING: THIS APPLICATION DOES NOT GUARANTEE 100% SECURITY WITH ALL YOUR PRIVATE SECRETS!)***
            """;

    public static final String DIARY_HELP_PREDEF = """
            <p>Looks like you&#39;ve just stumbled upon this application without knowing how to use it.</p>
            <p>Do you know what this application is in the first place?</p>
            <p>This diary uses Markdown as input to render an entry onto the screen.</p>
            <p>Use Ctrl+N to create a new entry, Ctrl+E to edit an entry, Ctrl+F to jump to an entry, Left and Right arrows to go back and forth entries.</p>
            <p><strong><em>(WARNING: THIS APPLICATION DOES NOT GUARANTEE 100% SECURITY WITH ALL YOUR PRIVATE SECRETS!)</em></strong></p>
            """;

    public static final String ENTRY_REMOVED_MESSAGE = """
            Well, there goes that entry... Why did you remove it?
            """;

    public static final String ENTRY_REMOVED_MESSAGE_PREDEF = """
            <p>Well, there goes that entry... Why did you remove it?</p>
            """;

    private static class DiaryEntryLoader extends SwingWorker<CacheRecord, Void> {
        private final String markdown;

        private DiaryEntryLoader(String markdown) {
            this.markdown = markdown;
        }

        @Override
        protected CacheRecord doInBackground() {
            DiaryFrame frame = Main.INSTANCE.frame;
            if (frame != null) {
                SwingUtilities.invokeLater(() -> frame.loadDialog.openLoadDialog());
            }
            setTask("Parsing...");
            setProgress(0);
            Node document = Parser.builder().build().parse(markdown);
            setTask("Converting into HTML...");
            setProgress(33);
            String rendered = HtmlRenderer.builder().build().render(document);
            setTask("Processing...");
            setProgress(66);
            ByteArrayInputStream bis = new ByteArrayInputStream(wrapHTMLIntoActualDocument(rendered).getBytes(StandardCharsets.UTF_8));
            CacheRecord record = new CacheRecord(rendered);
            record.time = System.currentTimeMillis();
            MD_TO_HTML_CACHE.put(markdown.hashCode(), record);
            setTask("Rendering...");
            setProgress(100);
            // uhhh too dangerous
            if (frame != null) {
                frame.updatePanelDirect(bis);
            }
            if (frame != null) {
                SwingUtilities.invokeLater(() -> frame.loadDialog.closeLoadDialog());
            }
            return record;
        }

        private void setTask(String task) {
            firePropertyChange("name", null, task);
        }
    }
}
