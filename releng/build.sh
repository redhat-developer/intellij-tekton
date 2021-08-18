go mod tidy
go build .
./tekton-jsongenerator
diff -q tekton.dev ../src/main/resources/schemas/tekton.dev
diff -q triggers.tekton.dev ../src/main/resources/schemas/triggers.tekton.dev
