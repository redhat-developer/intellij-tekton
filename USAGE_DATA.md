## [Tekton Pipelines by Red Hat](https://github.com/redhat-developer/intellij-tekton)

### Usage Data

* when plugin is started
* when an action is executed
    * action name
    * action duration time
    * action error message (in case of exception)
    * action specific data if applicable (see details below)
* when connecting to a cluster
  * Kubernetes version
  * flag for Kubernetes/OpenShift cluster
  * OpenShift version
* when plugin is shut down

### Actions operating on tekton resources
* resource kind
* resource schema version
* creation/update of a resource
