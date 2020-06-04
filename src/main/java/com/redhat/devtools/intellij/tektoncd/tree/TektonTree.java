package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualDocumentHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TektonTree extends Tree implements DataProvider {
    public TektonTree(TreeModel treemodel) {
        super(treemodel);
    }


    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (CommonDataKeys.NAVIGATABLE_ARRAY.is(dataId)) {
            TreePath[] paths = getSelectionPaths();
            if (paths == null) {
                return null;
            }


            List<Navigatable> result = new ArrayList<>();
            for (TreePath path : paths) {
                Object node = path.getLastPathComponent();
                ParentableNode<? extends ParentableNode<?>> element = StructureTreeAction.getElement(node);

                Pair<String, String> yamlAndKind = null;
                try {
                    yamlAndKind = TreeHelper.getYAMLAndKindFromNode(element);
                } catch (IOException e) {
                    UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
                }

                if (yamlAndKind != null && !yamlAndKind.first.isEmpty()) {
                    result.add(new TektonNavigatable(element, yamlAndKind.first, yamlAndKind.second));
                }
            }
            return result.isEmpty() ? null : result.toArray(new Navigatable[0]);
        }

        return null;
    }

    static class TektonNavigatable implements Navigatable {

        private final ParentableNode<?> node;
        private final String content;
        private final String kind;

        public TektonNavigatable(ParentableNode<?> node, String content, String kind) {
            this.node = node;
            this.content = content;
            this.kind = kind;
        }

        @Override
        public void navigate(boolean requestFocus) {
            Project project = node.getRoot().getProject();
            String namespace = node.getNamespace();
            Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                    filter(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + node.getName() + ".yaml") &&
                            fileEditor.getFile().getExtension().equals("yaml")).findFirst();
            if (!editor.isPresent()) {
                VirtualDocumentHelper.createAndOpenVirtualFile(project, namespace, namespace + "-" + node.getName() + ".yaml", content, kind);
            } else {
                FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            }

        }

        @Override
        public boolean canNavigate() {
            return true;
        }

        @Override
        public boolean canNavigateToSource() {
            return false;
        }
    }
}

