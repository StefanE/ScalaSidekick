#ScalaSidekick Plugin for Jedit

The goal of this plugin is to make Scala programmers more efficient.

Look at the downloads page to see latest release version [download](http://github.com/StefanE/ScalaSidekick/downloads)


##Implemented

- Reformat scala file using [Scalariform](http://github.com/mdr/scalariform) and defaults

##Current work
- Using a customized [Ensime Server](http://github.com/aemoncannon/ensime) as backend, a lot of stuff can be reused. If it is a success a lot of the planned features will be included in this implementation

##Planned

- Quick Navigation between Traits, Objects and Classes
- Comments
  - Scaladoc for definition on line
  - Insert new Scaladoc comment line
- Refactoring using [Scala Refactoring framework](http://scala-refactoring.org/)
  - Organize imports
  - Rename
  - Inline local
  - Extract Local
  - Extract method
- User controlled settings for reformat
- Integrate and optimize my [SIndex plugin](http://github.com/StefanE/jEdit-with-Scala)