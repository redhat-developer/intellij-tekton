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
	"reflect"
)

func arrayOrStringMapper(i reflect.Type) *jsonschema.Type {
	if (i == reflect.TypeOf(v1alpha1.ArrayOrString{}) || i == reflect.TypeOf(v1beta1.ArrayOrString{})) {
		return &jsonschema.Type{
			OneOf: []*jsonschema.Type{
				{
					Type: "string",
				},
				{
					Type: "array",
					Items: &jsonschema.Type{
						Type:                 "string",
					},
				},
			},
		}
	}
	return nil
}


func dump(v interface{}, apiVersion string, kind string) {
	fmt.Printf("Starting generation of %s %s\n", apiVersion, kind)
	filename := fmt.Sprintf("%s_%s.json", apiVersion, kind)
	reflector := jsonschema.Reflector{
		TypeMapper: arrayOrStringMapper,
	}
	reflect := reflector.Reflect(v)
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
	dump(&v1alpha1.Pipeline{}, "tekton.dev/v1alpha1", "Pipeline")
	dump(&v1alpha1.PipelineList{}, "tekton.dev/v1alpha1", "PipelineList")
	dump(&v1alpha1.PipelineRun{}, "tekton.dev/v1alpha1", "PipelineRun")
	dump(&v1alpha1.PipelineRunList{}, "tekton.dev/v1alpha1", "PipelineRunList")
	dump(&v1alpha1.Task{}, "tekton.dev/v1alpha1", "Task")
	dump(&v1alpha1.TaskList{}, "tekton.dev/v1alpha1", "TaskList")
	dump(&v1alpha1.TaskRun{}, "tekton.dev/v1alpha1", "TaskRun")
	dump(&v1alpha1.TaskRunList{}, "tekton.dev/v1alpha1", "TaskRunList")
	dump(&v1alpha1.ClusterTask{}, "tekton.dev/v1alpha1", "ClusterTask")
	dump(&v1alpha1.ClusterTaskList{}, "tekton.dev/v1alpha1", "ClusterTaskList")
	dump(&v1alpha1.Condition{}, "tekton.dev/v1alpha1", "Condition")
	dump(&v1alpha1.ConditionList{}, "tekton.dev/v1alpha1", "ConditionList")
	dump(&resource.PipelineResource{}, "tekton.dev/v1alpha1", "PipelineResource")
	dump(&resource.PipelineResourceList{}, "tekton.dev/v1alpha1", "PipelineResourceList")

	dump(&v1beta1.Pipeline{}, "tekton.dev/v1beta1", "Pipeline")
	dump(&v1beta1.PipelineList{}, "tekton.dev/v1beta1", "PipelineList")
	dump(&v1beta1.PipelineRun{}, "tekton.dev/v1beta1", "PipelineRun")
	dump(&v1beta1.PipelineRunList{}, "tekton.dev/v1beta1", "PipelineRunList")
	dump(&v1beta1.Task{}, "tekton.dev/v1beta1", "Task")
	dump(&v1beta1.TaskList{}, "tekton.dev/v1beta1", "TaskList")
	dump(&v1beta1.TaskRun{}, "tekton.dev/v1beta1", "TaskRun")
	dump(&v1beta1.TaskRunList{}, "tekton.dev/v1beta1", "TaskRunList")
	dump(&v1beta1.ClusterTask{}, "tekton.dev/v1beta1", "ClusterTask")
	dump(&v1beta1.ClusterTaskList{}, "tekton.dev/v1beta1", "ClusterTaskList")

	dump(&triggers.EventListener{}, "triggers.tekton.dev/v1alpha1", "EventListener")
	dump(&triggers.EventListenerList{}, "triggers.tekton.dev/v1alpha1", "EventListenerList")
	dump(&triggers.TriggerTemplate{}, "triggers.tekton.dev/v1alpha1", "TriggerTemplate")
	dump(&triggers.TriggerTemplateList{}, "triggers.tekton.dev/v1alpha1", "TriggerTemplateList")
	dump(&triggers.TriggerBinding{}, "triggers.tekton.dev/v1alpha1", "TriggerBinding")
	dump(&triggers.TriggerBindingList{}, "triggers.tekton.dev/v1alpha1", "TriggerBindingList")
	dump(&triggers.ClusterTriggerBinding{}, "triggers.tekton.dev/v1alpha1", "ClusterTriggerBinding")
	dump(&triggers.ClusterTriggerBindingList{}, "triggers.tekton.dev/v1alpha1", "ClusterTriggerBindingList")
}