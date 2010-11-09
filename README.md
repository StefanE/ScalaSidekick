#ScalaSidekick Plugin for Jedit

The goal of this plugin is to make Scala programmers more efficient.

Look at the downloads page to see latest release version [download](http://github.com/StefanE/ScalaSidekick/downloads)

##Current State
- Alpha, works when the user does the right things, not much error handling

##Implemented (Runs through Ensime)

- Reformat scala file using [Scalariform](http://github.com/mdr/scalariform) and defaults
- Organize Imports
- Rename
- Code completion
- Get Type info

##Current work
- Using a customized [Ensime Server](http://github.com/aemoncannon/ensime) as backend, a lot of stuff can be reused. If it is a success a lot of the planned features will be included in this implementation

##Known bugs
- Problems with autocompletion of imports...

##Planned

- Quick Navigation between Traits, Objects and Classes
- Comments
  - Scaladoc for definition on line
  - Insert new Scaladoc comment line
- Refactoring using [Scala Refactoring framework](http://scala-refactoring.org/)
  - Inline local
  - Extract Local
  - Extract method
- User controlled settings for reformat
- Integrate and optimize my [SIndex plugin](http://github.com/StefanE/jEdit-with-Scala)
- Nice GUI configuration
- Nicer Menu

## How to use it

Currently the plugin is a bit rough. It depends on that you use projectViewer. The project root should be pointing on a SBT project. 
When you have done that, you should Init Project, after some time it has intialized, and then you can use the implemented stuff:)

More will be added later