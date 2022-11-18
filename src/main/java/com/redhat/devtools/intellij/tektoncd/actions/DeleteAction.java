/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenerNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;
import com.redhat.devtools.intellij.tektoncd.ui.DeleteDialog;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.RefUsage;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class DeleteAction extends TektonAction {

    /**
     * The default code for the delete action.
     */
    public static final int OK_DELETE_CODE = 0;

    /**
     * The default code for "Cancel" action.
     */
    public static final int CANCEL_CODE = 1;

    /**
     * The default code for the delete item + related resources action.
     */
    public static final int OK_DELETE_RESOURCES_CODE = 2;

    public DeleteAction() {
        super(true,
            TaskNode.class,
            TaskRunNode.class,
            PipelineNode.class,
            PipelineRunNode.class,
            ClusterTaskNode.class,
            TriggerTemplateNode.class,
            TriggerBindingNode.class,
            ClusterTriggerBindingNode.class,
            EventListenerNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "delete resource");
        ParentableNode[] elements = Arrays.stream(selected).map(item -> getElement(item)).toArray(ParentableNode[]::new);

        ExecHelper.submit(() -> {
            String deleteText = getDeleteText(tkncli, elements);
            int resultDialog = UIHelper.executeInUI(() -> {
                DeleteDialog delDialog = new DeleteDialog(null, getTitle(elements), deleteText, getDeleteChkText(elements));
                delDialog.show();

                if (delDialog.isOK()) {
                    return delDialog.hasToDeleteRelatedResources() ? OK_DELETE_RESOURCES_CODE : OK_DELETE_CODE;
                }

                return CANCEL_CODE;
            });

            if (resultDialog != CANCEL_CODE) {
                String namespace = elements[0].getNamespace();
                boolean deleteRelatedResources = resultDialog == OK_DELETE_RESOURCES_CODE;
                Map<Class, List<ParentableNode>> resourcesByClass = TreeHelper.getResourcesByClass(elements);
                for(Class type: resourcesByClass.keySet()) {
                    try {
                        deleteResources(type, resourcesByClass, namespace, deleteRelatedResources, tkncli);
                        ((TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(resourcesByClass.get(type).get(0).getParent());
                        telemetry.property(TelemetryService.PROP_RESOURCE_KIND, type.getSimpleName())
                                .property(TelemetryService.PROP_RESOURCE_RELATED, String.valueOf(deleteRelatedResources))
                                .success()
                                .send();
                    } catch (IOException e) {
                        telemetry
                                .error(anonymizeResource(null, namespace, e.getMessage()))
                                .send();
                        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
                    }
                }
            }
        });
    }

    private String getTitle(ParentableNode[] elements) {
        if (elements.length == 1) {
            return "Delete " + elements[0].getName();
        } else {
            return "Delete multiple items";
        }
    }

    private String getDeleteText(Tkn tkn, ParentableNode[] elements) {
        StringBuilder sb = new StringBuilder("Are you sure you want to delete ");
        if (elements.length == 1) {
            String name = elements[0].getName();
            String kind = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "");
            int usages = getUsages(tkn, kind, name);
            if (usages > 0) {
                sb.insert(0, "This " + kind + " is being used by other resources. ");
            }
            sb.append(kind + " " + name + " ?");
        } else {
            sb.append("the following items?\n");
            for (ParentableNode element: elements) {
                String kind = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "");
                sb.append(element.getName() + getUsagesAsText(tkn, kind, element.getName()) + "\n");
            }
        }
        return sb.toString();
    }

    private String getDeleteChkText(ParentableNode[] elements) {
        if (elements.length == 1) {
            String kind = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "");
            return mayHaveRelatedResource(elements[0]) ? "Also delete its related resources (" + kind + "Runs)" : "";
        } else {
            return "Also delete their related resources (PipelineRuns, TaskRuns..)";
        }
    }

    private boolean mayHaveRelatedResource(ParentableNode element) {
        return element instanceof PipelineNode
                || element instanceof TaskNode
                || element instanceof ClusterTaskNode;
    }

    private String getUsagesAsText(Tkn tkn, String kind, String name) {
        int usages = getUsages(tkn, kind, name);
        if (usages == 0) {
            return "";
        }
        return " (found " + usages + " " + (usages == 1 ? "usage" : "usages") + ")";
    }

    private int getUsages(Tkn tkn, String kind, String name) {
        if (!(kind.equalsIgnoreCase(KIND_TASK) || kind.equalsIgnoreCase(KIND_CLUSTERTASK))) {
            return 0;
        }

        try {
            List<RefUsage> usages = tkn.findTaskUsages(kind, name);
            return usages.size();
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
            return 0;
        }
    }

    private void deleteResources(Class type, Map<Class, List<ParentableNode>> resourcesByClass, String namespace, boolean deleteRelatedResources, Tkn tkncli) throws IOException {
        List<String> resources = resourcesByClass.get(type).stream().map(x -> x.getName()).collect(Collectors.toList());
        if (type.equals(PipelineNode.class)) {
            tkncli.deletePipelines(namespace, resources, deleteRelatedResources);
        } else if (type.equals(PipelineRunNode.class)) {
            tkncli.deletePipelineRuns(namespace, resources);
        } else if (type.equals(TaskNode.class)) {
            tkncli.deleteTasks(namespace, resources, deleteRelatedResources);
        } else if (type.equals(ClusterTaskNode.class)) {
            tkncli.deleteClusterTasks(resources, deleteRelatedResources);
        } else if (type.equals(TaskRunNode.class)) {
            tkncli.deleteTaskRuns(namespace, resources);
        } else if (type.equals(TriggerTemplateNode.class)) {
            tkncli.deleteTriggerTemplates(namespace, resources);
        } else if (type.equals(TriggerBindingNode.class)) {
            tkncli.deleteTriggerBindings(namespace, resources);
        } else if (type.equals(ClusterTriggerBindingNode.class)) {
            tkncli.deleteClusterTriggerBindings(resources);
        } else if (type.equals(EventListenerNode.class)) {
            tkncli.deleteEventListeners(namespace, resources);
        }
    }
}
