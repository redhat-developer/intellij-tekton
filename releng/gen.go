package main

import (
	"bytes"
	"fmt"
	"github.com/redhat-developer/tekton-jsongenerator/jsonschema"
	v1alpha1 "github.com/tektoncd/pipeline/pkg/apis/pipeline/v1alpha1"
	"github.com/tektoncd/pipeline/pkg/apis/pipeline/v1beta1"
	resource "github.com/tektoncd/pipeline/pkg/apis/resource/v1alpha1"
	triggers "github.com/tektoncd/triggers/pkg/apis/triggers/v1alpha1"
	"os"
	"encoding/json"
)


func dump(v interface{}, apiVersion string, kind string, list bool) {
	fmt.Printf("Starting generation of %s %s\n", apiVersion, kind)
	var realKind string
	if realKind = kind; list {
		realKind = kind + "List"
	}
	filename := fmt.Sprintf("%s_%s.json", apiVersion, realKind)
	reflect := jsonschema.Reflect(v)
	JSON, _ := reflect.MarshalJSON()
	file, _ := os.Create(filename)
	defer file.Close()
	var out bytes.Buffer
	json.Indent(&out, JSON, "", "  ")
	out.WriteTo(file)
	index, _ := os.OpenFile("index.properties", os.O_WRONLY|os.O_APPEND, 0)
	index.WriteString(filename)
	index.WriteString("\n")
}

func main() {
	os.Create("index.properties")
	dump(&v1alpha1.Pipeline{}, "tekton.dev/v1alpha1", "Pipeline", false)
	dump(&v1alpha1.PipelineList{}, "tekton.dev/v1alpha1", "Pipeline", true)
	dump(&v1alpha1.PipelineRun{}, "tekton.dev/v1alpha1", "PipelineRun", false)
	dump(&v1alpha1.PipelineRunList{}, "tekton.dev/v1alpha1", "PipelineRun", true)
	dump(&v1alpha1.Task{}, "tekton.dev/v1alpha1", "Task", false)
	dump(&v1alpha1.TaskList{}, "tekton.dev/v1alpha1", "Task", true)
	dump(&v1alpha1.TaskRun{}, "tekton.dev/v1alpha1", "TaskRun", false)
	dump(&v1alpha1.TaskRunList{}, "tekton.dev/v1alpha1", "TaskRun", true)
	dump(&v1alpha1.ClusterTask{}, "tekton.dev/v1alpha1", "ClusterTask", false)
	dump(&v1alpha1.ClusterTaskList{}, "tekton.dev/v1alpha1", "ClusterTask", true)
	dump(&v1alpha1.Condition{}, "tekton.dev/v1alpha1", "Condition", false)
	dump(&v1alpha1.ConditionList{}, "tekton.dev/v1alpha1", "Condition", true)
	dump(&resource.PipelineResource{}, "tekton.dev/v1alpha1", "PipelineResource", false)
	dump(&resource.PipelineResourceList{}, "tekton.dev/v1alpha1", "PipelineResource", true)

	dump(&v1beta1.Pipeline{}, "tekton.dev/v1beta1", "Pipeline", false)
	dump(&v1beta1.PipelineList{}, "tekton.dev/v1beta1", "Pipeline", true)
	dump(&v1beta1.PipelineRun{}, "tekton.dev/v1beta1", "PipelineRun", false)
	dump(&v1beta1.PipelineRunList{}, "tekton.dev/v1beta1", "PipelineRun", true)
	dump(&v1beta1.Task{}, "tekton.dev/v1beta1", "Task", false)
	dump(&v1beta1.TaskList{}, "tekton.dev/v1beta1", "Task", true)
	dump(&v1beta1.TaskRun{}, "tekton.dev/v1beta1", "TaskRun", false)
	dump(&v1beta1.TaskRunList{}, "tekton.dev/v1beta1", "TaskRun", true)
	dump(&v1beta1.ClusterTask{}, "tekton.dev/v1beta1", "ClusterTask", false)
	dump(&v1beta1.ClusterTaskList{}, "tekton.dev/v1beta1", "ClusterTask", true)

	dump(&triggers.EventListener{}, "triggers.tekton.dev/v1alpha1", "EventListener", false)
	dump(&triggers.EventListenerList{}, "triggers.tekton.dev/v1alpha1", "EventListener", true)
	dump(&triggers.TriggerTemplate{}, "triggers.tekton.dev/v1alpha1", "TriggerTemplate", false)
	dump(&triggers.TriggerTemplateList{}, "triggers.tekton.dev/v1alpha1", "TriggerTemplate", true)
	dump(&triggers.TriggerBinding{}, "triggers.tekton.dev/v1alpha1", "TriggerBinding", false)
	dump(&triggers.TriggerBindingList{}, "triggers.tekton.dev/v1alpha1", "TriggerBinding", true)
	dump(&triggers.ClusterTriggerBinding{}, "triggers.tekton.dev/v1alpha1", "ClusterTriggerBinding", false)
	dump(&triggers.ClusterTriggerBindingList{}, "triggers.tekton.dev/v1alpha1", "ClusterTriggerBinding", true)

}


