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
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            List<LookupElementBuilder> lookups;
            // check if there is an opened $( (without a closing parenthesis) before the position we are right now. It matches $( or $(params or $(whatever
            Matcher matcher = Pattern.compile("\\$\\([^\\)]*$").matcher(result.getPrefixMatcher().getPrefix());
            if (matcher.find()) {
                lookups = getInputsLookups(parameters, matcher.group(), result.getPrefixMatcher().getPrefix(), matcher.start() + 2);
            } else {
                lookups = getGenericLookups();
            }
            result.addAllElements(lookups);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }
    }

    private List<LookupElementBuilder> getInputsLookups(CompletionParameters parameters, String prefix, String completionPrefix, int insertOffset) {
        String configuration = parameters.getEditor().getDocument().getText();
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        if (model == null) return Collections.emptyList();

        return getLookupsByKind(model, prefix, completionPrefix, insertOffset);
    }

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

    private List<LookupElementBuilder> getLookupsPipeline(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        // if prefix is not valid for a pipeline
        String headPrefix = prefix.length() > 9 ? prefix.substring(0, 9) : prefix;
        if ("$(params.".contains(headPrefix)) {
            model.getParams().stream().forEach(param -> {
                String lookup = "params." + param.name();
                lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                        .withPresentableText(lookup)
                        .withLookupString(lookup)
                        .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
            });
        }
        return lookups;
    }

    private List<LookupElementBuilder> getLookupsTask(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        // if prefix is not valid for a task
        String headPrefix_9 = prefix.length() > 9 ? prefix.substring(0, 9) : prefix;
        if ("$(params.".contains(headPrefix_9)) {
            model.getParams().stream().forEach(param -> {
                String lookup = "params." + param.name();
                lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                        .withPresentableText(lookup)
                        .withLookupString(lookup)
                        .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
            });
        }

        String headPrefix_12 = prefix.length() > 12 ? prefix.substring(0, 12) : prefix;
        String headPrefix_19 = prefix.length() > 19 ? prefix.substring(0, 19) : prefix;
        if ("$(resources.".contains(headPrefix_12) || "$(resources.inputs.".contains(headPrefix_19)) {
            String resourceInput = getResource("resources.inputs", prefix);
            if (resourceInput != null) {
                Optional<String> type = model.getInputResources().stream().filter(input -> input.name().equals(resourceInput)).map(input -> input.type()).findFirst();
                if (type.isPresent()) {
                    lookups.addAll(getLookupsByResource("resources.inputs." + resourceInput, type.get(), completionPrefix, insertOffset));
                }
            } else {
                model.getInputResources().stream().forEach(resource -> {
                    String lookup = "resources.inputs." + resource.name();
                    lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                            .withPresentableText(lookup)
                            .withLookupString(lookup)
                            .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
                });
            }
        }

        String headPrefix_20 = prefix.length() > 20 ? prefix.substring(0, 20) : prefix;
        if ("$(resources.".contains(headPrefix_12) || "$(resources.outputs.".contains(headPrefix_20)) {
            // if a workspace is already selected (e.g $(workspaces.name.), let's show code completion for workspace (path, claim ...)
            String resourceOutput = getResource("resources.outputs", prefix);
            if (resourceOutput != null) {
                Optional<String> type = model.getOutputResources().stream().filter(output -> output.name().equals(resourceOutput)).map(output -> output.type()).findFirst();
                if (type.isPresent()) {
                    lookups.addAll(getLookupsByResource("resources.outputs." + resourceOutput, type.get(), completionPrefix, insertOffset));
                }
            } else {
                model.getOutputResources().stream().forEach(resource -> {
                    String lookup = "resources.outputs." + resource.name();
                    lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                            .withPresentableText(lookup)
                            .withLookupString(lookup)
                            .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
                });
            }
        }

        String headPrefix_13 = prefix.length() > 13 ? prefix.substring(0, 13) : prefix;
        if ("$(workspaces.".contains(headPrefix_13)) {
            // if a workspace is already selected (e.g $(workspaces.name.), let's show code completion for workspace (path, claim ...)
            String workspace = getResource("workspaces", prefix);
            if (workspace != null) {
                lookups.addAll(getLookupsByWorkspace("workspaces." + workspace, completionPrefix, insertOffset));
            } else {
                model.getWorkspaces().stream().forEach(workspaceName -> {
                    String lookup = "workspaces." + workspaceName;
                    lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                            .withPresentableText(lookup)
                            .withLookupString(lookup)
                            .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
                });
            }
        }

        return lookups;
    }

    private List<LookupElementBuilder> getLookupsCondition(ConfigurationModel model, String prefix, String completionPrefix, int insertOffset) {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        // if prefix is not valid for a condition
        String headPrefix_12 = prefix.length() > 12 ? prefix.substring(0, 12) : prefix;
        String headPrefix_9 = prefix.length() > 9 ? prefix.substring(0, 9) : prefix;
        if ("$(params.".contains(headPrefix_9)) {
            model.getParams().stream().forEach(param -> {
                String lookup = "params." + param.name();
                lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                        .withPresentableText(lookup)
                        .withLookupString(lookup)
                        .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
            });
        }

        if ("$(resources.".contains(headPrefix_12)) {
            String resourceInput = getResource("resources", prefix);
            if (resourceInput != null) {
                Optional<String> type = model.getInputResources().stream().filter(input -> input.name().equals(resourceInput)).map(input -> input.type()).findFirst();
                if (type.isPresent()) {
                    lookups.addAll(getLookupsByResource("resources." + resourceInput, type.get(), completionPrefix, insertOffset));
                }
            } else {
                model.getInputResources().stream().forEach(resource -> {
                    String lookup = "resources." + resource.name();
                    lookups.add(LookupElementBuilder.create(completionPrefix + lookup)
                            .withPresentableText(lookup)
                            .withLookupString(lookup)
                            .withInsertHandler(new VariableCompletionAutoInsertHandler(lookup, insertOffset)));
                });
            }
        }

        return lookups;
    }

    private List<LookupElementBuilder> getGenericLookups() throws IOException {
        return TknDictionary.get().getLookups();
    }

    private String getResource(String type, String prefix) {
        Matcher matcher = Pattern.compile("\\$\\(" + type + "\\.([^.]+)\\.").matcher(prefix);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

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
