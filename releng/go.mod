module github.com/redhat-developer/tekton-jsongenerator

go 1.14

require (
	github.com/alecthomas/jsonschema v0.0.0-20200217214135-7152f22193c9
	github.com/iancoleman/orderedmap v0.0.0-20190318233801-ac98e3ecb4b0
	github.com/tektoncd/pipeline v0.15.1
	github.com/tektoncd/triggers v0.5.0 // indirect
)

replace (
	k8s.io/api => k8s.io/api v0.16.5
	k8s.io/apimachinery => k8s.io/apimachinery v0.16.5
	k8s.io/cli-runtime => k8s.io/cli-runtime v0.16.5
	k8s.io/client-go => k8s.io/client-go v0.16.5
)
