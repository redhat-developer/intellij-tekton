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
package com.redhat.devtools.intellij.tektoncd.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.ConditionConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provider is called for code completion that does not belong to a specific place in the file.
 */
public class GeneralCompletionProvider extends CompletionProvider<CompletionParameters> {
    Logger logger = LoggerFactory.getLogger(GeneralCompletionProvider.class);

    private static final String[] workspaceVariables = new String[]{ "path", "claim", "volume" };
    private static final String[] pullRequestResourcesVariables = new String[]{ "name", "type", "url", "provider", "insecure-skip-tls-verify", "path" };
    private static final String[] imageResourcesVariables = new String[]{ "name", "type", "url", "digest", "path" };
    private static final String[] gitResourcesVariables = new String[]{ "name", "type", "url", "revision", "refspec", "depth", "sslVerify", "httpProxy", "httpsProxy", "noProxy", "path" };
    private static final String[] gcsResourcesVariables = new String[]{ "name", "type", "location", "path" };
    private static final String[] clusterResourcesVariables = new String[]{ "name", "type", "url", "username", "password", "namespace", "token", "insecure", "cadata", "clientKeyData", "clientCertificateData", "path" };
    private static final String[] cloudEventResourcesVariables = new String[]{ "name", "type", "target-uri", "path" };

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        try {
            List<LookupElementBuilder> lookups = Collections.emptyList();
            // check if there is an opened $( (without a closing parenthesis) before the position we are right now. It matches $( or $(params or $(whatever
            Matcher matcher = Pattern.compile("\\$\\([^\\)]*$").matcher(result.getPrefixMatcher().getPrefix());
            if (matcher.find()) {
                lookups = getInputsLookups(parameters, matcher.group(), result.getPrefixMatcher().getPrefix(), matcher.start() + 2);
            } else if (parameters.getEditor().getDocument().getText().trim().isEmpty()) {
                // if the document is a yaml file and it's empty, let's show generic lookups
                lookups = getGenericLookups();
            }
            result.addAllElements(lookups);
            result.stopHere();
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }
    }

    /**
     * Get lookups for the current parameter
     *
     * @param parameters
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getInputsLookups(CompletionParameters parameters, String prefix, String completionPrefix, int insertOffset) {
        String configuration = parameters.getEditor().getDocument().getText();
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        if (model == null) return Collections.emptyList();

        return getLookupsByKind(model, prefix, completionPrefix, insertOffset);
    }

    /**
     * Get lookups for the current parameter based on the kind
     *
     * @param model the model built by the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsByKind(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        switch (model.getKind().toLowerCase()) {
            case "pipeline":
                return getLookupsPipeline(model, prefix, completionPrefix, insertOffset);
            case  "task":
                return getLookupsTask(model, prefix, completionPrefix, insertOffset);
            case "condition":
                return getLookupsCondition(model, prefix, completionPrefix, insertOffset);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Get lookups for the opened pipeline configuration
     *
     * @param model the model built by the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsPipeline(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        return getParamLookups(((PipelineConfigurationModel)model).getParams(), prefix, completionPrefix, insertOffset);
    }

    /**
     * Get lookups for the opened task configuration
     *
     * @param model the model built by the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsTask(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        // get lookups for params
        lookups.addAll(getParamLookups(((TaskConfigurationModel)model).getParams(), prefix, completionPrefix, insertOffset));

        // get lookups for resources
        String headPrefix_19 = prefix.length() > 19 ? prefix.substring(0, 19) : prefix;
        if ("$(resources.inputs.".contains(headPrefix_19)) {
            lookups.addAll(getInputResourceLookups(((TaskConfigurationModel)model).getInputResources(), "resources.inputs", prefix, completionPrefix, insertOffset));
        }

        // get lookups for output resources
        String headPrefix_20 = prefix.length() > 20 ? prefix.substring(0, 20) : prefix;
        if ("$(resources.outputs.".contains(headPrefix_20)) {
            lookups.addAll(getOutputResourceLookups(((TaskConfigurationModel)model).getOutputResources(), "resources.outputs", prefix, completionPrefix, insertOffset));
        }
        // get lookups for workspaces
        lookups.addAll(getWorkspaceLookups(((TaskConfigurationModel)model).getWorkspaces(), prefix, completionPrefix, insertOffset));

        return lookups;
    }

    /**
     * Get lookups for the opened condition configuration
     *
     * @param model the model built by the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsCondition(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();

        lookups.addAll(getParamLookups(((ConditionConfigurationModel)model).getParams(), prefix, completionPrefix, insertOffset));

        String headPrefix_12 = prefix.length() > 12 ? prefix.substring(0, 12) : prefix;
        if ("$(resources.".contains(headPrefix_12)) {
            lookups.addAll(getInputResourceLookups(((ConditionConfigurationModel)model).getInputResources(), "resources", prefix, completionPrefix, insertOffset));
        }

        return lookups;
    }

    /**
     * Get lookups for params
     *
     * @param params params present in the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getParamLookups(List<Input> params, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        String headPrefix = prefix.length() > 9 ? prefix.substring(0, 9) : prefix;
        if ("$(params.".contains(headPrefix)) {
            params.stream().forEach(param -> {
                lookups.add(createInnerLookup("params." + param.name(), completionPrefix, insertOffset));
            });
        }
        return lookups;
    }

    /**
     * Get lookups for input resources
     *
     * @param resources input resources found in the configuration
     * @param typeLabel label representing the resource type (e.g resource/resource.input)
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getInputResourceLookups(List<Input> resources, String typeLabel, String prefix, String completionPrefix, int insertOffset) {
        // check if a resource has already been picked up and we need to show specific resource completion (path, name, ...) - e.g $(resources.inputs.foo.
        String resourceInput = getResource(typeLabel, prefix);
        Optional<String> type = Optional.empty();
        if (resourceInput != null) {
            type = resources.stream().filter(input -> input.name().equals(resourceInput)).map(input -> input.type()).findFirst();
        }
        return getInnerResourceLookups(resources.stream().map(resource -> resource.name()).collect(Collectors.toList()), typeLabel, type, resourceInput, completionPrefix, insertOffset);
    }

    /**
     * Get lookups for output resources
     *
     * @param resources input resources found in the configuration
     * @param typeLabel label representing the resource type (e.g resource/resource.input)
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getOutputResourceLookups(List<Output> resources, String typeLabel, String prefix, String completionPrefix, int insertOffset) {
        // check if a resource has already been picked up and we need to show specific resource completion (path, name, ...) - e.g $(resources.outputs.foo.
        String resourceOutput = getResource(typeLabel, prefix);
        Optional<String> type = Optional.empty();
        if (resourceOutput != null) {
            type = resources.stream().filter(output -> output.name().equals(resourceOutput)).map(output -> output.type()).findFirst();
        }
        return getInnerResourceLookups(resources.stream().map(resource -> resource.name()).collect(Collectors.toList()), typeLabel, type, resourceOutput, completionPrefix, insertOffset);
    }

    /**
     * inner function to build resource lookups
     *
     * @param resources list of resources names found in configuration
     * @param typeLabel label representing the resource type (e.g resource/resource.input/resources.output)
     * @param type resource type if already added by the user
     * @param resourceName resource name if already added by the user
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getInnerResourceLookups(List<String> resources, String typeLabel, Optional<String> type, String resourceName, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        if (type.isPresent()) {
            lookups.addAll(getLookupsByResource(typeLabel + "." + resourceName, type.get(), completionPrefix, insertOffset));
        } else {
            resources.stream().forEach(resource -> {
                lookups.add(createInnerLookup(typeLabel + "." + resource, completionPrefix, insertOffset));
            });
        }
        return lookups;
    }

    /**
     * Get lookups for workspace
     *
     * @param workspaces workspaces found in the configuration
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params." -> the prefix is "$(params."
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getWorkspaceLookups(List<String> workspaces, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        String headPrefix_13 = prefix.length() > 13 ? prefix.substring(0, 13) : prefix;
        if ("$(workspaces.".contains(headPrefix_13)) {
            // check if a workspace has already been picked up and we need to show specific workspace completion (path, claim, ...) - e.g $(workspaces.foo.
            String workspace = getResource("workspaces", prefix);
            if (workspace != null) {
                lookups.addAll(getLookupsByWorkspace("workspaces." + workspace, completionPrefix, insertOffset));
            } else {
                workspaces.stream().forEach(workspaceName -> {
                    lookups.add(createInnerLookup("workspaces." + workspaceName, completionPrefix, insertOffset));
                });
            }
        }
        return lookups;
    }

    private LookupElementBuilder createInnerLookup(String name, String completionPrefix, int insertOffset) {
        return LookupElementBuilder.create(completionPrefix + name)
                .withPresentableText(name)
                .withLookupString(name)
                .withInsertHandler(new VariableCompletionAutoInsertHandler(name, insertOffset));
    }

    private List<LookupElementBuilder> getGenericLookups() throws IOException {
        return TknDictionary.get().getLookups();
    }

    /**
     * Retrieve the name of a resource
     *
     * @param type the resource type (workspaces/resource.input/resource.output....)
     * @param prefix the prefix we are really using. E.g the line we are in is "value: test -f $(params.foo." -> the prefix is "$(params.foo."
     * @return the name of the resource. E.g (type: params, prefix: $(params.foo.) -> foo
     */
    private String getResource(String type, String prefix) {
        Matcher matcher = Pattern.compile("\\$\\(" + type + "\\.([^.]+)\\.").matcher(prefix);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * It return lookups for a workspace
     *
     * @param workspace workspace name
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsByWorkspace(String workspace, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        Arrays.stream(workspaceVariables).forEach(variable -> {
            String lookup = workspace + "." + variable;
            lookups.add(LookupElementBuilder.create(completionPrefix + variable)
                    .withPresentableText(variable)
                    .withLookupString(variable)
                    .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
        });
        return lookups;
    }

    /**
     * it returns lookups for a resource based on its type
     *
     * @param resource resource name
     * @param type resource type
     * @param completionPrefix the prefix we need to add to the lookup to make it be shown by IJ. E.g the line we are in is "value: test -f $(params." -> the completionPrefix is "test -f $(params."
     * @param insertOffset the position where the lookup has to be copied on
     * @return
     */
    private List<LookupElementBuilder> getLookupsByResource(String resource, String type, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        String[] variables = getVariablesByType(type);
        if (variables == null) return Collections.emptyList();

        Arrays.stream(variables).forEach(variable -> {
            String lookup = resource + "." + variable;
            lookups.add(LookupElementBuilder.create(completionPrefix + variable)
                    .withPresentableText(variable)
                    .withLookupString(variable)
                    .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
        });

        return lookups;
    }

    private String[] getVariablesByType(String type) {
        switch (type.toLowerCase()) {
            case "git":
                return gitResourcesVariables;
            case "pullrequest":
                return pullRequestResourcesVariables;
            case "image":
                return imageResourcesVariables;
            case "gcs":
            case "buildgcs":
                return gcsResourcesVariables;
            case "cluster":
                return clusterResourcesVariables;
            case "cloudevent":
                return cloudEventResourcesVariables;
            default:
                return null;
        }
    }
}
