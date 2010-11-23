package org.ensime.server
import java.io.File
import org.ensime.config.ProjectConfig
import org.ensime.debug.ProjectDebugInfo
import org.ensime.model._
import org.ensime.protocol.ProtocolConst._
import org.ensime.util._
import scala.actors._
import scala.actors.Actor._
import scala.collection.immutable
import scala.tools.nsc.io.AbstractFile
import scala.tools.refactoring.common.Change
import scalariform.formatter.ScalaFormatter
import scalariform.parser.ScalaParserException

trait RPCTarget { self: Project =>

  import protocol._

  def rpcInitProject(conf: ProjectConfig, callId: Int) {
    initProject(conf)
    sendRPCReturn(toWF(conf), callId)
  }

  def rpcPeekUndo(callId: Int) {
    peekUndo match {
      case Right(result) => sendRPCReturn(toWF(result), callId)
      case Left(msg) => sendRPCError(ErrPeekUndoFailed, Some(msg), callId)
    }
  }

  def rpcExecUndo(undoId: Int, callId: Int) {
    execUndo(undoId) match {
      case Right(result) => sendRPCReturn(toWF(result), callId)
      case Left(msg) => sendRPCError(ErrExecUndoFailed, Some(msg), callId)
    }
  }

  def rpcReplConfig(callId: Int) {
    sendRPCReturn(toWF(this.config.replConfig), callId)
  }

  def rpcDebugConfig(callId: Int) {
    debugInfo = Some(new ProjectDebugInfo(config))
    sendRPCReturn(toWF(this.config.debugConfig), callId)
  }

  def rpcBuilderInit(callId: Int) {
    val b = getOrStartBuilder
    b ! RPCRequestEvent(RebuildAllReq(), callId)
  }

  def rpcBuilderAddFiles(filenames: Iterable[String], callId: Int) {
    val files = filenames.map(s => new File(s.toString))
    val b = getOrStartBuilder
    b ! RPCRequestEvent(AddSourceFilesReq(files), callId)
  }

  def rpcBuilderUpdateFiles(filenames: Iterable[String], callId: Int) {
    val files = filenames.map(s => new File(s.toString))
    val b = getOrStartBuilder
    b ! RPCRequestEvent(UpdateSourceFilesReq(files), callId)
  }

  def rpcBuilderRemoveFiles(filenames: Iterable[String], callId: Int) {
    val files = filenames.map(s => new File(s.toString))
    val b = getOrStartBuilder
    b ! RPCRequestEvent(RemoveSourceFilesReq(files), callId)
  }

  def rpcDebugUnitInfo(sourceName: String, line: Int, packPrefix: String, callId: Int) {
    val info = debugInfo.getOrElse(new ProjectDebugInfo(config))
    debugInfo = Some(info)
    val unit = info.findUnit(sourceName, line, packPrefix)
    unit match {
      case Some(unit) => {
        sendRPCReturn(toWF(unit), callId)
      }
      case None => {
        sendRPCReturn(toWF(false), callId)
      }
    }
  }

  def rpcDebugClassLocsToSourceLocs(pairs: Iterable[(String, Int)], callId: Int) {
    val info = debugInfo.getOrElse(new ProjectDebugInfo(config))
    debugInfo = Some(info)
    sendRPCReturn(
      toWF(info.debugClassLocsToSourceLocs(pairs)),
      callId)
  }

  def rpcTypecheckFile(f: String, callId: Int) {
    val file: File = new File(f)
    analyzer ! RPCRequestEvent(ReloadFileReq(file), callId)
  }

  def rpcTypecheckAll(callId: Int) {
    analyzer ! RPCRequestEvent(ReloadAllReq(), callId)
  }

  def rpcScopeCompletion(f: String, point: Int, prefix: String, constructor: Boolean, callId: Int) {
    analyzer ! RPCRequestEvent(ScopeCompletionReq(new File(f), point, prefix, constructor), callId)
  }

  def rpcTypeCompletion(f: String, point: Int, prefix: String, callId: Int) {
    analyzer ! RPCRequestEvent(TypeCompletionReq(new File(f), point, prefix), callId)
  }

  def rpcPackageMemberCompletion(path: String, prefix: String, callId: Int) {
    analyzer ! RPCRequestEvent(PackageMemberCompletionReq(path, prefix), callId)
  }

  def rpcInspectTypeAtPoint(f: String, point: Int, callId: Int) {
    analyzer ! RPCRequestEvent(InspectTypeReq(new File(f), point), callId)
  }

  def rpcInspectTypeById(id: Int, callId: Int) {
    analyzer ! RPCRequestEvent(InspectTypeByIdReq(id), callId)
  }

  def rpcSymbolAtPoint(f: String, point: Int, callId: Int) {
    analyzer ! RPCRequestEvent(SymbolAtPointReq(new File(f), point), callId)
  }

  def rpcTypeById(id: Int, callId: Int) {
    analyzer ! RPCRequestEvent(TypeByIdReq(id), callId)
  }

  def rpcTypeByName(name: String, callId: Int) {
    analyzer ! RPCRequestEvent(TypeByNameReq(name), callId)
  }

  def rpcTypeByNameAtPoint(name: String, f: String, point: Int, callId: Int) {
    analyzer ! RPCRequestEvent(TypeByNameAtPointReq(name, new File(f), point), callId)
  }

  def rpcCallCompletion(id: Int, callId: Int) {
    analyzer ! RPCRequestEvent(CallCompletionReq(id), callId)
  }

  def rpcImportSuggestions(f: String, point: Int, names: List[String], callId: Int) {
    analyzer ! RPCRequestEvent(ImportSuggestionsReq(new File(f), point, names), callId)
  }

  def rpcTypeAtPoint(f: String, point: Int, callId: Int) {
    analyzer ! RPCRequestEvent(TypeAtPointReq(new File(f), point), callId)
  }

  def rpcInspectPackageByPath(path: String, callId: Int) {
    analyzer ! RPCRequestEvent(InspectPackageByPathReq(path), callId)
  }

  def rpcPerformRefactor(refactorType: Symbol, procId: Int, params: immutable.Map[Symbol, Any], callId: Int) {
    analyzer ! RPCRequestEvent(RefactorPerformReq(procId, refactorType, params), callId)
  }

  def rpcExecRefactor(refactorType: Symbol, procId: Int, callId: Int) {
    analyzer ! RPCRequestEvent(RefactorExecReq(procId, refactorType), callId)
  }

  def rpcCancelRefactor(procId: Int, callId: Int) {
    analyzer ! RPCRequestEvent(RefactorCancelReq(procId), callId)
  }

  def rpcFormatFiles(filenames: Iterable[String], callId: Int) {
    val files = filenames.map { new File(_) }
    try {
      val changeList = files.map { f =>
        FileUtils.readFile(f) match {
          case Right(contents) => {
            val formatted = ScalaFormatter.format(contents, config.formattingPrefs)
            Change(AbstractFile.getFile(f), 0, contents.length, formatted)
          }
          case Left(e) => throw e
        }
      }
      addUndo("Formatted source of " + filenames.mkString(", ") + ".", 
	FileUtils.inverseChanges(changeList))
      FileUtils.writeChanges(changeList) match {
        case Right(_) => sendRPCAckOK(callId)
        case Left(e) =>
          sendRPCError(ErrFormatFailed, 
	    Some("Could not write any formatting changes: " + e), callId)
      }
    } catch {
      case e: ScalaParserException =>
        sendRPCError(ErrFormatFailed, 
	  Some("Cannot format broken syntax: " + e), callId)
    }

  }

}
