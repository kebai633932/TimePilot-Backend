package org.cxk.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;

public class MarkdownUtils {

    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * Markdown 转 HTML
     */
    public static String mdToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    /**
     * Markdown 转纯文本
     */
    public static String mdToPlainText(String markdown) {
        String html = mdToHtml(markdown);
        return Jsoup.parse(html).text(); // 去掉所有标签
    }
}
