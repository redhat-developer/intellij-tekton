module github.com/redhat-developer/tekton-jsongenerator

go 1.14

require (
	github.com/iancoleman/orderedmap v0.0.0-20190318233801-ac98e3ecb4b0
	github.com/tektoncd/pipeline v0.32.0
	github.com/tektoncd/triggers v0.17.1
	k8s.io/apiextensions-apiserver v0.22.5
	k8s.io/apimachinery v0.22.5
	knative.dev/pkg v0.0.0-20220104185830-52e42b760b54
)

replace (
	k8s.io/api => k8s.io/api v0.21.4
	k8s.io/apimachinery => k8s.io/apimachinery v0.21.4
	k8s.io/cli-runtime => k8s.io/cli-runtime v0.21.4
	k8s.io/client-go => k8s.io/client-go v0.21.4
)
