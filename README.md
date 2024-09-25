
[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/14096-tekton-pipelines-by-red-hat?label=Jet%20Brains%20Marketplace&style=for-the-badge)](https://plugins.jetbrains.com/plugin/14096-tekton-pipelines-by-red-hat)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/14096-tekton-pipelines-by-red-hat?logo=jetbrains&style=for-the-badge)](https://plugins.jetbrains.com/plugin/14096-tekton-pipelines-by-red-hat)
[![Java CI with Gradle](https://img.shields.io/github/actions/workflow/status/redhat-developer/intellij-tekton/ci.yml?label=Java%20CI%20with%20Gradle&style=for-the-badge)](https://github.com/redhat-developer/intellij-tekton/actions/workflows/ci.yml?query=workflow%3ACI)
[![Validate against IJ versions](https://img.shields.io/github/actions/workflow/status/redhat-developer/intellij-tekton/IJ.yml?label=IJ%20Versions&style=for-the-badge)](https://github.com/redhat-developer/intellij-tekton/actions/workflows/IJ.yml)
[![License](https://img.shields.io/github/license/redhat-developer/intellij-tekton?style=for-the-badge)](https://github.com/redhat-developer/intellij-tekton/blob/main/LICENSE)

# IntelliJ Tekton

## NOTE: This plugin is no longer maintained and is officially deprecated.

## Overview

Tekton Pipelines extension provides an end-to-end developer experience for interaction with [Tekton](https://tekton.dev/).

Using this extension:

   * Developer can create Tekton resource easily by using `Deploy resource on save`.
   * Install Task/ClusterTask from `TektonHub`.
   * Support for start Pipeline, Workspace and create new PVC(PersistentVolumeClaim) using `wizard workflow`.
   * Show Tekton TaskRun/PipelineRun `Logs in Editor`.
   * Support for `Adding Trigger`.
   * Tekton variables `code completion`.
   * Pipeline/PipelineRun `preview diagram`

![](images/demo1.gif)

## Features

For more detail information around specific commands & features, please read the [plugin features](README.features.md) detailed guide.


## Tekton Versions

- The maximum IDEA version supported is now 2024.1.
- The minimum IDEA version supported is now 2022.3.
- The Tekton CLI in use has been upgraded to 0.27.0.
- Schemas for validation and code assist have been updated to Tekton Pipeline `0.56.0` and Tekton Triggers `0.26.0`.

Note: We support `v1beta1` API. Previous version `v1alpha1` may work, but we cannot guarantee that all features will work properly. If you have `v1alpha1` pipelines/tasks please use [migrating document](https://github.com/tektoncd/pipeline/blob/main/docs/migrating-v1alpha1-to-v1beta1.md) to migrate to `v1beta1`.
### Dependencies

This plugin uses a CLI tool to interact with Tekton Pipelines:
* Tekton CLI - [tkn](https://github.com/tektoncd/cli)

> The plugin will detect these dependencies and prompt the user to install if they are missing or have not supported version - choose `Download & Install` when you see an notification for the missing tool.

**NOTE:** This plugin is in Preview mode. The plugin support for Tekton is strictly experimental - assumptions may break, commands and behavior may change!

### Release notes

See the [release notes](https://github.com/redhat-developer/intellij-tekton/releases).

### Contributing

This is an open source project open to anyone. This project welcomes contributions and suggestions!

For information on getting started, refer to the [CONTRIBUTING instructions](CONTRIBUTING.md).

### Feedback & Questions

If you discover an issue please file a bug and we will fix it as soon as possible.
* File a bug in [GitHub Issues](https://github.com/redhat-developer/intellij-tekton/issues).
* Open a [Discussion on GitHub](https://github.com/redhat-developer/intellij-tekton/discussions).

If you want to chat with us, you can contact us on the `#ide-integration` channel of the `tektoncd` Slack. Please click this [link](https://join.slack.com/t/tektoncd/shared_invite/enQtNjQ1NjQzNTQ3MDQwLTc5MWU4ODg3MGJiYjllZjlmMWI0YWFlMzJjMTkyZGEyMTFhYzY1ZTkzZGU0M2I3NGEyYjU2YzNhOTE4OWQyZTM) to join the `tektoncd` Slack.

### License

EPL 2.0, See [LICENSE](LICENSE) for more information.

### Data and Telemetry

The JetBrains IntelliJ Tekton plugin collects anonymous [usage data](USAGE_DATA.md) and sends it to Red Hat servers to help improve our products and services. Read our [privacy statement](https://developers.redhat.com/article/tool-data-collection) to learn more. This extension respects the Red Hat Telemetry setting which you can learn more about at [https://github.com/redhat-developer/intellij-redhat-telemetry#telemetry-reporting](https://github.com/redhat-developer/intellij-redhat-telemetry#telemetry-reporting)
