# Thread Access plugin

Simple plugin dedicated for Intellij plugin developers. Provides information about thread access during plugin debugging.

Shows information about:
<ul>
    <li>Is write Thread</li>
    <li>Is write access allowed</li>
    <li>Is read access allowed</li>
    <li>Is dispatch Thread</li>
</ul>

There is no configuration. Plugin will try to detect if a process is 
debugging Intellij plugin and provide proper thread access information.

## Installation

You can install this plugin from JetBrains plugin repository.