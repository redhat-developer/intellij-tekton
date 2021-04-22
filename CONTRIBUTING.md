# How to contribute

Contributions are essential for keeping this plugin great.
We try to keep it as easy as possible to contribute changes and we are
open to suggestions for making it even easier.
There are only a few guidelines that we need contributors to follow.

## First Time Setup
1. Install prerequisites:
   * [Java Development Kit](https://adoptopenjdk.net/)
2. Fork and clone the repository
3. `cd intellij-tekton`
4. Import the folder as a project in JetBrains IntelliJ

## Run the plugin locally

1. From root folder, run the below command.
    ```bash
    $ ./gradlew runIde
    ```


2. Once the plugin is installed and reloaded, there will be a Tekton Tab in the Tool Windows list (left).

> If you have any questions or run into any problems, please post an issue - we'll be very happy to help.


## Commit Messages
Commit messages on main branches must follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary). Contributors are encouraged to use them as well, but maintainers will happily fix things up when merging pull requests if needed.

The Conventional Commits specification is a lightweight convention on top of commit messages, which allow us to automate the release process. This convention dovetails with SemVer, by describing the features, fixes, and breaking changes made in commit messages. 

Your commit messages should be structured as follows:

```
<type>[optional scope]: <description>
```

Read more about [commit types](https://github.com/pvdlg/conventional-commit-types#commit-types).

