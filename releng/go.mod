module github.com/redhat-developer/tekton-jsongenerator

go 1.14

require (
	github.com/alecthomas/jsonschema v0.0.0-20200217214135-7152f22193c9
	github.com/iancoleman/orderedmap v0.0.0-20190318233801-ac98e3ecb4b0
	github.com/tektoncd/pipeline v0.25.0
	github.com/tektoncd/triggers v0.14.2
	k8s.io/apiextensions-apiserver v0.19.7
	k8s.io/apimachinery v0.19.7
	k8s.io/test-infra v0.0.0-20200828131253-b23899a92dfa // indirect
	knative.dev/pkg v0.0.0-20210331065221-952fdd90dbb0
	sigs.k8s.io/boskos v0.0.0-20200819010710-984516eae7e8 // indirect
)

replace (
	k8s.io/api => k8s.io/api v0.18.8
	k8s.io/apimachinery => k8s.io/apimachinery v0.18.8
	k8s.io/cli-runtime => k8s.io/cli-runtime v0.18.8
	k8s.io/client-go => k8s.io/client-go v0.18.8
)
