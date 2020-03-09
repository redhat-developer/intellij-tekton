package com.redhat.devtools.intellij.tektoncd.tkn;

public class Resource {
    private String name;
    private String type;
    private String paths;

    public Resource(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public String paths() { return paths; }

    public void setPaths(String paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return name;
    }
}
