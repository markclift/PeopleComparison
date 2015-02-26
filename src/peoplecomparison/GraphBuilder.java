package peoplecomparison;

import com.itextpdf.text.*;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.*;
import org.gephi.io.exporter.preview.*;
import org.gephi.layout.plugin.*;
import org.gephi.layout.plugin.forceAtlas.*;
import org.gephi.preview.api.*;
import org.gephi.preview.types.*;
import org.gephi.project.api.*;
import org.openide.util.*;

import java.awt.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * Manages the node graph model (in memory)
 * Created by Mark Clift on 25/02/15. Lots borrowed from: https://github.com/palmerabollo/test-twitter-graph/blob/master/src/main/java/es/guido/twitter/graph/GraphBuilder.java
 */
public class GraphBuilder {
    private GraphModel graphModel;
    private static final float SIZE_CORRECTION_FACTOR = 0.5f;

    public GraphBuilder() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // Get a graph model - it exists because we have a workspace
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
    }

    public void setSize(String idNode, float size) {
        size = SIZE_CORRECTION_FACTOR * (float) Math.log10(1 + size);

        if (size < 1f) {
            size += 1f;
        }

        Node node = findOrCreateNode(idNode);
        node.getNodeData().setSize(size);
    }

    public void setAlpha(String idNode, float alpha) {
        Node node = findOrCreateNode(idNode);
        node.getNodeData().setAlpha(alpha);
    }

    public void setColor(String idNode, float r, float g, float b) {
        Node node = findOrCreateNode(idNode);
        node.getNodeData().setColor(r, g, b);
    }

    /*
    public void addDirectedRelation(String idSource, String idTarget) {
        addDirectedRelation(idSource, idTarget, 1f);
    }*/

    public void addUndirectedRelation(String idSource, String idTarget, double weight) {
        System.out.println(idSource + " <-- (" + weight + ") --> " + idTarget);
        Node source = findOrCreateNode(idSource);
        Node target = findOrCreateNode(idTarget);
        buildEdge(source, target, (float)weight);
    }

    public void export(String format, String fileName) throws IOException {
        System.out.println("Exporting as " + format + " to " + fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        configurePreview();

        configureLayout();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        PDFExporter pdfExporter = (PDFExporter) ec.getExporter(format);
        pdfExporter.setPageSize(PageSize.A0);
        pdfExporter.setWorkspace(workspace);

        ec.exportStream(fos, pdfExporter);
        fos.flush();
    }

    private void configureLayout() {
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);

        ForceAtlasLayout layout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f); // True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(500.), 0f); // 500 for the complete period
        autoLayout.addLayout(layout, 1f, new AutoLayout.DynamicProperty[]{ adjustBySizeProperty, repulsionProperty });

        autoLayout.execute();
    }

    private void configurePreview() {
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_OUTLINE_COLOR, new DependantColor(Color.LIGHT_GRAY));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.BLACK));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_SHOW_BOX, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_BOX_OPACITY, 50);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_BOX_COLOR, new DependantColor(Color.LIGHT_GRAY));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 2f);
        previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
        previewController.refreshPreview();
    }

    private Node findOrCreateNode(String id) {
        Graph graph = graphModel.getGraph();
        Node result = graph.getNode(id);
        if (result == null) {
            result = buildNode(id);
            graph.addNode(result);
        }
        return result;
    }

    private Node buildNode(String id) {
        Node result = graphModel.factory().newNode(id);
        result.getNodeData().setLabel(id);
        return result;
    }

    private Edge buildEdge(Node n1, Node n2, float weight) {
        Edge result = graphModel.factory().newEdge(n1, n2, weight, false); // false specifies undirected
        graphModel.getUndirectedGraph().addEdge(result);
        return result;
    }
/*
    public void removeLowWeightEdges() {
        int edgeCount = graphModel.getUndirectedGraph().getEdgeCount();
        Edge[] edges = graphModel.getUndirectedGraph().getEdges().toArray();
        for (int i = 0; i < edgeCount; i++) {
            Edge edge = edges[i];
            if (edge.getWeight() < 0.5) graphModel.getUndirectedGraph().removeEdge(edge);
        }
    }*/
}

