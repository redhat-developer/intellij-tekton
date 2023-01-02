module github.com/redhat-developer/tekton-jsongenerator

go 1.14

require (
	github.com/iancoleman/orderedmap v0.0.0-20190318233801-ac98e3ecb4b0
	github.com/tektoncd/pipeline v0.43.0
	github.com/tektoncd/triggers v0.22.0
	k8s.io/apiextensions-apiserver v0.25.3
	k8s.io/apimachinery v0.25.4
	knative.dev/pkg v0.0.0-20221011175852-714b7630a836
)

replace cloud.google.com/go/compute => cloud.google.com/go/compute v1.12.1
