package org.ensime.config

import java.io.File
import org.ensime.util._
import org.ensime.util.FileUtils._
import org.ensime.util.RichFile._
import org.ensime.util.SExp._
import scala.actors._
import scala.actors.Actor._
import scala.collection.mutable
import scalariform.formatter.preferences._
import org.ensime.protocol.message.InitProject

object ProjectConfig {

  //SCALAPROTOCOL------

  import org.ensime.protocol._

  def apply(msg: InitProject): ProjectConfig = {
    import ExternalConfigInterface._
    println("ROOTDIR:" + msg.rootDir)
    val rootDir = CanonFile(new File(msg.rootDir))

    val sourceRoots = new mutable.HashSet[CanonFile]
    val runtimeDeps = new mutable.HashSet[CanonFile]
    val compileDeps = new mutable.HashSet[CanonFile]
    val classDirs = new mutable.HashSet[CanonFile]
    var target: Option[CanonFile] = None
    println("w" + rootDir)
    println("w" + msg.rootDir)
    val ext = getSbtConfig(rootDir, Iterable[String]())
    sourceRoots ++= ext.sourceRoots
    runtimeDeps ++= ext.runtimeDepJars
    compileDeps ++= ext.compileDepJars
    compileDeps ++= ext.testDepJars
    target = ext.target

    val formatPrefs = Map[Symbol, Any]()

    if (sourceRoots.isEmpty) {
      val f = new File("src")
      if (f.exists && f.isDirectory) {
        println("Using default source root, 'src'.")
        sourceRoots += f
      }
    }

    new ProjectConfig(
      rootDir, sourceRoots, runtimeDeps,
      compileDeps, classDirs, target,
      formatPrefs)
  }

  def nullConfig = new ProjectConfig(new File("."), List(),
    List(), List(), List(), None, Map())

  def getJavaHome(): Option[File] = {
    val javaHome: String = System.getProperty("java.home");
    if (javaHome == null) None
    else Some(new File(javaHome))
  }

  def javaBootJars(): Set[CanonFile] = {
    val javaHome = getJavaHome();
    javaHome match {
      case Some(javaHome) => {
        if (System.getProperty("os.name").startsWith("Mac")) {
          expandRecursively(
            new File("."),
            List(new File(javaHome, "../Classes")),
            isValidJar)
        } else {
          expandRecursively(
            new File("."),
            List(new File(javaHome, "lib")),
            isValidJar)
        }
      }
      case None => Set()
    }
  }

}

class ReplConfig(val classpath: String) {}

class DebugConfig(val classpath: String, val sourcepath: String) {}

class ProjectConfig(
                     val root: CanonFile,
                     val sourceRoots: Iterable[CanonFile],
                     val runtimeDeps: Iterable[CanonFile],
                     val compileDeps: Iterable[CanonFile],
                     val classDirs: Iterable[CanonFile],
                     val target: Option[CanonFile],
                     formattingPrefsMap: Map[Symbol, Any]) {

  val formattingPrefs = formattingPrefsMap.
    foldLeft(FormattingPreferences()) {
    (fp, p) =>
      p match {
        case ('alignParameters, value: Boolean) =>
          fp.setPreference(AlignParameters, value)
        case ('compactStringConcatenation, value: Boolean) =>
          fp.setPreference(CompactStringConcatenation, value)
        case ('doubleIndentClassDeclaration, value: Boolean) =>
          fp.setPreference(DoubleIndentClassDeclaration, value)
        case ('formatXml, value: Boolean) =>
          fp.setPreference(FormatXml, value)
        case ('indentSpaces, value: Int) =>
          fp.setPreference(IndentSpaces, value)
        case ('preserveSpaceBeforeArguments, value: Boolean) =>
          fp.setPreference(PreserveSpaceBeforeArguments, value)
        case ('rewriteArrowSymbols, value: Boolean) =>
          fp.setPreference(RewriteArrowSymbols, value)
        case ('spaceBeforeColon, value: Boolean) =>
          fp.setPreference(SpaceBeforeColon, value)
        case (name, _) => {
          System.err.println("Oops, unrecognized formatting option: " + name)
          fp
        }
      }
  }

  def compilerClasspathFilenames: Set[String] = {
    (compileDeps ++ classDirs).map(_.getPath).toSet
  }

  def sources: Set[CanonFile] = {
    expandRecursively(root, sourceRoots, isValidSourceFile _).toSet
  }

  def sourceFilenames: Set[String] = {
    sources.map(_.getPath).toSet
  }

  def compilerArgs = List(
    "-classpath", compilerClasspath,
    "-verbose",
    sourceFilenames.mkString(" ")
  )

  def builderArgs = List(
    "-classpath", compilerClasspath,
    "-verbose",
    "-d", target.getOrElse(new File(root, "classes")).getPath,
    sourceFilenames.mkString(" ")
  )

  def compilerClasspath: String = {
    compilerClasspathFilenames.mkString(File.pathSeparator)
  }

  def runtimeClasspath: String = {
    val allFiles = compileDeps ++ runtimeDeps ++ classDirs ++ target
    val paths = allFiles.map(_.getPath).toSet
    paths.mkString(File.pathSeparator)
  }

  def replClasspath = runtimeClasspath

  def debugClasspath = runtimeClasspath

  def debugSourcepath = {
    sourceRoots.map(_.getPath).toSet.mkString(File.pathSeparator)
  }

  def replConfig = new ReplConfig(replClasspath)

  def debugConfig = new DebugConfig(debugClasspath, debugSourcepath)

}

