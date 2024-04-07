package com.github.brickwall2900.diary.html;

import com.github.brickwall2900.diary.DiaryException;
import com.github.brickwall2900.diary.DiaryFrame;
import com.github.brickwall2900.diary.DiaryStore;
import com.github.brickwall2900.diary.Main;
import com.github.brickwall2900.diary.dialogs.DiaryLoadDialog;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.brickwall2900.diary.utils.TranslatableText.text;

public class DiaryMarkdownToHTML {
    private static class CacheRecord {
        final String content;
        long time;

        private CacheRecord(String content) {
            this.content = content;
        }
    }

    public static final Map<Integer, CacheRecord> MD_TO_HTML_CACHE = new HashMap<>();

    private static final String TEMPLATE, LIGHT_CSS, DARK_CSS;

    static {
        try (InputStream template = DiaryMarkdownToHTML.class.getResourceAsStream("/html/template.xhtml");
             InputStream lightCSS = DiaryMarkdownToHTML.class.getResourceAsStream("/html/light.css");
             InputStream darkCSS = DiaryMarkdownToHTML.class.getResourceAsStream("/html/dark.css")) {
            TEMPLATE = new String(template.readAllBytes());
            LIGHT_CSS = new String(lightCSS.readAllBytes());
            DARK_CSS = new String(darkCSS.readAllBytes());
        } catch (IOException e) {
            throw new DiaryException("HTML Template not found!", e);
        }

        List<Extension> extensions = new ArrayList<>();
        extensions.add(AutolinkExtension.create());
        extensions.add(StrikethroughExtension.create());
        extensions.add(TablesExtension.create());
        extensions.add(InsExtension.create());
        extensions.add(ImageAttributesExtension.create());
        extensions.add(TaskListItemsExtension.create());
        PARSER = Parser.builder().extensions(extensions).build();
        RENDERER = HtmlRenderer.builder().extensions(extensions).build();
    }


    public static String getHTMLFromMarkdown(String md) {
        int givenHash = md.hashCode();
        CacheRecord record = MD_TO_HTML_CACHE.get(givenHash);
        String toReturn;
        if (record != null) {
            record.time = System.currentTimeMillis();
            toReturn = record.content;
        } else {
            DiaryEntryLoader entryLoader = getDiaryEntryLoader(md);
            entryLoader.execute();
            toReturn = wrapHTMLIntoActualDocument(text("html.loader.message"));
        }
        MD_TO_HTML_CACHE.entrySet().removeIf(entry -> (System.currentTimeMillis() - entry.getValue().time) >= entry.getValue().content.length() * 60L);
        return toReturn;
    }

    private static DiaryEntryLoader getDiaryEntryLoader(String md) {
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
        return entryLoader;
    }

    public static String wrapHTMLIntoActualDocument(String html) {
        String r =  TEMPLATE.replace("/* stylesheet! */", DiaryStore.CONFIGURATION.darkMode ? DARK_CSS : LIGHT_CSS).replace("<!-- content! -->", html);
        return r;
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

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

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
            setTask(text("html.process.parse"));
            setProgress(0);
            Node document = PARSER.parse(markdown);
            setTask(text("html.process.convert"));
            setProgress(33);
            String rendered = RENDERER.render(document);
            setTask(text("html.process.process"));
            setProgress(66);
            ByteArrayInputStream bis = new ByteArrayInputStream(wrapHTMLIntoActualDocument(rendered).getBytes(Charset.defaultCharset()));
            CacheRecord record = new CacheRecord(rendered);
            record.time = System.currentTimeMillis();
            MD_TO_HTML_CACHE.put(markdown.hashCode(), record);
            setTask(text("html.process.render"));
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
