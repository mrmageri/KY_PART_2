package config;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public final class PngRenderer {

    private static final String HEADLESS_KEY = "java.awt.headless";
    private static final String HEADLESS_VALUE = "true";
    private static final String PNG_FORMAT = "png";
    private static final String FONT_NAME = Font.SANS_SERIF;
    private static final String ELLIPSIS = "...";
    private static final int ELLIPSIS_LENGTH = 3;
    private static final int NODE_HEIGHT = 40;
    private static final int H_GAP = 30;
    private static final int V_GAP = 60;
    private static final int MARGIN = 30;
    private static final int FONT_SIZE = 14;
    private static final int ARC = 12;
    private static final int LINE_THICKNESS = 2;
    private static final int BORDER_THICKNESS = 2;
    private static final int MAX_LABEL = 30;
    private static final int NODE_WIDTH = 220;
    private static final int RGB_BG = 0xFFFFFF;
    private static final int RGB_EDGE = 0x555555;
    private static final int RGB_NODE_FILL = 0xE8F0FE;
    private static final int RGB_NODE_BORDER = 0x1A73E8;
    private static final int RGB_TEXT = 0x202124;
    private static final int HALF = 2;
    private static final int NO_GAPS = 0;
    private static final int ROOT_DEPTH = 0;
    private static final int DEPTH_STEP = 1;
    private static final int START_INDEX = 0;
    private static final int INDEX_STEP = 1;

    static {
        System.setProperty(HEADLESS_KEY, HEADLESS_VALUE);
    }

    private PngRenderer() {
    }

    public static void render(Map<String, List<String>> graph, String root,
                              String pngFile) throws ConfigException {
        List<List<String>> levels = computeLevels(graph, root);
        if (levels.isEmpty()) {
            throw new ConfigException("Cannot render PNG: graph is empty");
        }
        Map<String, Point> positions = computePositions(levels);
        int width = computeWidth(levels);
        int height = computeHeight(levels);

        BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(RGB_BG));
            g.fillRect(0, 0, width, height);
            drawEdges(g, graph, positions);
            drawNodes(g, positions);
        } finally {
            g.dispose();
        }

        try {
            ImageIO.write(image, PNG_FORMAT, Path.of(pngFile).toFile());
            System.out.println("\n[PNG saved to " + pngFile + "]");
        } catch (IOException e) {
            throw new ConfigException(
                    "Failed to write PNG file: " + pngFile, e);
        }
    }

    private static List<List<String>> computeLevels(
            Map<String, List<String>> graph, String root) {
        Map<String, Integer> depth = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        if (graph.containsKey(root)) {
            depth.put(root, ROOT_DEPTH);
            queue.add(root);
        }
        while (!queue.isEmpty()) {
            String node = queue.poll();
            int currentDepth = depth.get(node);
            for (String dep : graph.getOrDefault(node, List.of())) {
                if (!depth.containsKey(dep)) {
                    depth.put(dep, currentDepth + DEPTH_STEP);
                    queue.add(dep);
                }
            }
        }
        if (depth.isEmpty()) {
            return List.of();
        }
        int maxDepth = ROOT_DEPTH;
        for (int d : depth.values()) {
            if (d > maxDepth) {
                maxDepth = d;
            }
        }
        List<List<String>> levels = new ArrayList<>();
        for (int i = START_INDEX; i <= maxDepth; i += INDEX_STEP) {
            levels.add(new ArrayList<>());
        }
        for (Map.Entry<String, Integer> entry : depth.entrySet()) {
            levels.get(entry.getValue()).add(entry.getKey());
        }
        return levels;
    }

    private static Map<String, Point> computePositions(
            List<List<String>> levels) {
        Map<String, Point> positions = new HashMap<>();
        int width = computeWidth(levels);
        for (int depth = START_INDEX; depth < levels.size(); depth += INDEX_STEP) {
            List<String> level = levels.get(depth);
            int levelWidth = levelWidth(level.size());
            int startX = (width - levelWidth) / HALF;
            int y = MARGIN + depth * (NODE_HEIGHT + V_GAP);
            for (int i = START_INDEX; i < level.size(); i += INDEX_STEP) {
                int x = startX + i * (NODE_WIDTH + H_GAP);
                positions.put(level.get(i), new Point(x, y));
            }
        }
        return positions;
    }

    private static int levelWidth(int count) {
        int gaps = Math.max(NO_GAPS, count - INDEX_STEP);
        return count * NODE_WIDTH + gaps * H_GAP;
    }

    private static int computeWidth(List<List<String>> levels) {
        int max = NO_GAPS;
        for (List<String> level : levels) {
            int w = levelWidth(level.size());
            if (w > max) {
                max = w;
            }
        }
        return max + MARGIN + MARGIN;
    }

    private static int computeHeight(List<List<String>> levels) {
        return MARGIN + MARGIN + levels.size() * (NODE_HEIGHT + V_GAP);
    }

    private static void drawEdges(Graphics2D g,
                                  Map<String, List<String>> graph,
                                  Map<String, Point> positions) {
        g.setColor(new Color(RGB_EDGE));
        g.setStroke(new BasicStroke(LINE_THICKNESS));
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            Point from = positions.get(entry.getKey());
            if (from == null) {
                continue;
            }
            int fromX = from.x + NODE_WIDTH / HALF;
            int fromY = from.y + NODE_HEIGHT;
            for (String dep : entry.getValue()) {
                Point to = positions.get(dep);
                if (to == null) {
                    continue;
                }
                int toX = to.x + NODE_WIDTH / HALF;
                int toY = to.y;
                g.drawLine(fromX, fromY, toX, toY);
            }
        }
    }

    private static void drawNodes(Graphics2D g, Map<String, Point> positions) {
        g.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        FontMetrics fm = g.getFontMetrics();
        for (Map.Entry<String, Point> entry : positions.entrySet()) {
            Point p = entry.getValue();
            g.setColor(new Color(RGB_NODE_FILL));
            g.fillRoundRect(p.x, p.y, NODE_WIDTH, NODE_HEIGHT, ARC, ARC);
            g.setColor(new Color(RGB_NODE_BORDER));
            g.setStroke(new BasicStroke(BORDER_THICKNESS));
            g.drawRoundRect(p.x, p.y, NODE_WIDTH, NODE_HEIGHT, ARC, ARC);
            g.setColor(new Color(RGB_TEXT));
            String label = shorten(entry.getKey());
            int textWidth = fm.stringWidth(label);
            int x = p.x + (NODE_WIDTH - textWidth) / HALF;
            int y = p.y + (NODE_HEIGHT - fm.getHeight()) / HALF + fm.getAscent();
            g.drawString(label, x, y);
        }
    }

    private static String shorten(String label) {
        if (label.length() <= MAX_LABEL) {
            return label;
        }
        return label.substring(START_INDEX, MAX_LABEL - ELLIPSIS_LENGTH)
                + ELLIPSIS;
    }
}