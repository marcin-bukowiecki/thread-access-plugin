# Thread Access plugin

Simple plugin dedicated for Intellij plugin developers. Provides information about thread access during plugin debugging.

Shows information about:
<ul>
    <li>Is write Thread</li>
    <li>Is write access allowed</li>
    <li>Is read access allowed</li>
    <li>Is dispatch Thread</li>
    <li>Is disposed (in Java source code)</li>
    <li>Is in impatient reader</li>
    <li>Holds read lock</li>
</ul>

There is no configuration. Plugin will try to detect if a process is 
debugging Intellij plugin and provide proper thread access information.

## Installation

You can install this plugin from JetBrains plugin repository: [link](https://plugins.jetbrains.com/plugin/16815-thread-access-info)

## Contributing

Feel free to do any contributions. If you want to test/debug it just clone this repository.

After cloning, build it with Gradle and run the plugin with <code>runIde</code> task.

From the running sandbox process create a plugin project (<code>File</code>|<code>New</code>|<code>Project</code>|<code>Intellij Platform Plugin</code>) and run it (even if this is a blank plugin).

At this moment you will have two running sandboxes but the 2nd was launched from the 1st. In 2nd sandbox create a sample project.
In the 1st sandbox place a breakpoint (e.g. in <code>com.intellij.openapi.application.impl.ApplicationImpl.runWriteAction</code> method). 
After that in the 2nd sandbox hit save on a source file (e.g. java) with <code>ctrl+s</code> or just edit it. 
