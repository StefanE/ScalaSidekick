#ScalaSidekick Plugin for Jedit

The goal of this plugin is to make Scala programmers more efficient.

Look at the downloads page to see latest release version [download](http://github.com/StefanE/ScalaSidekick/downloads)

##Current State
- Alpha, works with an SBT project, so far only used in single project environment

##Implemented (Runs through Ensime)

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
- Exact navigation (Enter name on class,object or trait). Should be improved to look more like OpenIt.

##Current work
- Using a customized [Ensime Server](http://github.com/aemoncannon/ensime) as backend, a lot of stuff can be reused. If it is a success a lot of the planned features will be included in this implementation

##Known problems
- Problems with autocompletion of imports...
- Completion dont filter when user types characters
- Index should be optimized

##Planned

- Comments
  - Scaladoc for definition on line
  - Insert new Scaladoc comment line
- Refactoring using [Scala Refactoring framework](http://scala-refactoring.org/)
  - Inline local
  - Extract Local
  - Extract method
  
##Nice to have (wont happen anytime soon)

- User controlled settings for reformat
- Integrate and optimize my [SIndex plugin](http://github.com/StefanE/jEdit-with-Scala)
- Nice GUI configuration
- Nicer Menu

## How to use it

Will soon put up a screencast showing the usage.

More will be added later