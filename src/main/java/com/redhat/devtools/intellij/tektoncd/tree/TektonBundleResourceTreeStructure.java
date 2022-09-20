/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TektonBundleResourceTreeStructure extends TektonTreeStructure {
    public TektonBundleResourceTreeStructure(Project project) {
        super(project);
    }

    @Override
    public Object[] getChildElements(Object element) {
        Tkn tkn = root.getTkn();
        if (tkn != null) {
            if (element instanceof TektonRootNode) {
                return getFirstLevelNodes((TektonRootNode) element);
            }
            if (element instanceof NamespaceNode) {
                Object[] generalNodes = new Object[]{
                        new PipelinesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ResourcesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ConditionsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                if (!tkn.isTektonTriggersAware()) {
                    return generalNodes;
                }
                Object[] triggersNode = new Object[] {
                        new TriggerTemplatesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new EventListenersNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                return ArrayUtils.addAll(generalNodes, triggersNode);

            }
            if (element instanceof PipelinesNode) {
                return getPipelines((PipelinesNode) element);
            }
            if (element instanceof TasksNode) {
                return getTasks((TasksNode) element);
            }
            if (element instanceof ClusterTasksNode) {
                return getClusterTasks((ClusterTasksNode) element);
            }
            if (element instanceof ResourcesNode) {
                return getResources((ResourcesNode) element);
            }
            if (element instanceof ConditionsNode) {
                return getConditions((ConditionsNode)element);
            }
            if (element instanceof TriggerTemplatesNode) {
                return getTriggerTemplates((TriggerTemplatesNode) element);
            }
            if (element instanceof TriggerBindingsNode) {
                return getTriggerBindings((TriggerBindingsNode) element);
            }
            if (element instanceof ClusterTriggerBindingsNode) {
                return getClusterTriggerBindingsNode((ClusterTriggerBindingsNode) element);
            }
            if (element instanceof EventListenersNode) {
                return getEventListenersNode((EventListenersNode) element);
            }
        }
        return new Object[0];
    }

    private Object[] getFirstLevelNodes(TektonRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            Tkn tkn = element.getTkn();
            if (tkn.isTektonAware()) {
                String namespace = element.getTkn().getNamespace();
                namespaces.add(new NamespaceNode(element, namespace));
            } else {
                namespaces.add(new MessageNode(element, element, NO_TEKTON));
            }
        } catch (IOException e) {
            namespaces.add(new MessageNode(element, element, ERROR));
        }
        return namespaces.toArray(new Object[namespaces.size()]);
    }


}
