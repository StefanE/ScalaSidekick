#ScalaSidekick Plugin for Jedit

The goal of this plugin is to make Scala programmers more efficient.

Look at the downloads page to see latest release version [download](http://github.com/StefanE/ScalaSidekick/downloads)

##Current State
- Alpha, works with an SBT project, so far only used in single project environment

##Implemented so far:

###Refactoring
- Reformat scala file using [Scalariform](http://github.com/mdr/scalariform) and defaults
- Organize Imports 
- Rename

###Code assist
- Code completion
- Get Type info
- Find definition
- Error highlighting at file and project level 

### Misc
- Exact navigation (Enter name on class,object or trait). Should be improved to look more like the plugin OpenIt.
- Scala REPL

##Current work
- Using a customized [Ensime Server](http://github.com/aemoncannon/ensime) as backend, a lot of stuff can be reused. If it is a success a lot of the planned features will be included in this implementation
- Currently Im have stopped my feature implementation and trying to stabilize the current features.

##Known problems (Should be moved to issues)
- Problems with autocompletion of imports...
- Index should be optimized

##Planned

- Comments
  - Scaladoc for definition on line
  - Insert new Scaladoc comment line
- More refactoring using [Scala Refactoring framework](http://scala-refactoring.org/)
  - Inline local
  - Extract Local
  - Extract method
  
##Nice to have (wont happen anytime soon)

- User controlled settings for reformat
- Integrate and optimize my [SIndex plugin](http://github.com/StefanE/jEdit-with-Scala)
- Nice GUI configuration
- Nicer Menu

## How to use it

Here is the guide, and a link to a screencast (Guide)[http://groups.google.com/group/liftweb/browse_thread/thread/3e66b5a0b2791879]
