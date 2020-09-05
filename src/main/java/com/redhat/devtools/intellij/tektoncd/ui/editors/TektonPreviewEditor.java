/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.editors;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeListener;

public class TektonPreviewEditor extends UserDataHolderBase implements FileEditor, DocumentListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TektonPreviewEditor.class);


    private final VirtualFile file;
    private final Document document;
    private final mxGraph graph;
    private final GraphUpdater graphUpdater;
    private JComponent pane;

    /*
     DataFlavor use the context class loader but not the caller class loader so won't find the JGraphX classes
     so we need to extend the initialization
     */
    static {
        try {
            mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                    + "; class=com.mxgraph.swing.util.mxGraphTransferable", null,
                    mxGraphTransferable.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private void loadModel() {
        Object content = graphUpdater.adapt(document.getText());
        if (content != null) {
            graph.setModel(new mxGraphModel());
            Object parent = graph.getDefaultParent();
            graph.getModel().beginUpdate();
            graphUpdater.update(content, graph);
            mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
            layout.setUseBoundingBox(false);
            layout.execute(parent);
            graph.getModel().endUpdate();
        }
    }

    TektonPreviewEditor(Project project, VirtualFile file, GraphUpdater graphUpdater) {
        FileDocumentManager manager = FileDocumentManager.getInstance();
        this.file = file;
        this.document = manager.getDocument(file);
        this.graphUpdater = graphUpdater;
        document.addDocumentListener(this, this);
        graph = new mxGraph();
        loadModel();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() > 0) {
                    graphComponent.zoomOut();
                } else {
                    graphComponent.zoomIn();
                }
            }
        });
        graphComponent.getConnectionHandler().setEnabled(false);
        pane = new JBScrollPane(graphComponent);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return pane;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return "Tekton preview";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        loadModel();
    }
}
