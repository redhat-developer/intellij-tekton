package com.redhat.devtools.intellij.tektoncd.tkn;

public class Resource {
    private String name;
    private String type;

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

    @Override
    public String toString() {
        return name;
    }
}
