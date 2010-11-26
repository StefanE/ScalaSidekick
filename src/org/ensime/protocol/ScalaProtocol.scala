package org.ensime.protocol

import java.io._
import org.ensime.config.{ProjectConfig, DebugConfig, ReplConfig}
import org.ensime.debug.{DebugUnit, DebugSourceLinePairs}
import org.ensime.model._
import org.ensime.server._
import org.ensime.util._
import scala.actors._
import org.ensime.protocol.message._
import org.ensime.client._
import org.scala.sidekick.ScalaSidekickPlugin._

object ScalaProtocol extends ScalaProtocol {}

trait ScalaProtocol extends Protocol {

  val PROTOCOL_VERSION: String = "0.0.1"

  val SERVER_NAME: String = "ENSIMEserver jEdit version"

  val HEADERSIZE = 50

  private var outPeer: Actor = null
  private var rpcTarget: RPCTarget = null

  //Should be protected?

  def peer: Actor = outPeer

  /* Below implementation of trait Protocol */

  def setOutputActor(peer: Actor) {
    outPeer = peer
  }

  def setRPCTarget(target: RPCTarget) {
    this.rpcTarget = target
  }

  def writeMessage(value: WireFormat, out: Writer) {
    ClientReceiver ! value
  }

  def readMessage(in: Reader): WireFormat = {
    throw new Exception("Should not happen")
  }

  def handleIncomingMessage(msg: Any) {
    msg match {
      case GetConnectionInfo(msgID) => sendConnectionInfo(msgID)
      case e: InitProject => {
        val conf = ProjectConfig(e)
        rpcTarget.rpcInitProject(conf, 1000)
      }
      case TypeAtPoint(file, offset, id) => {
        rpcTarget.rpcTypeAtPoint(file, offset, id)
      }
      case TypeCompletion(file, offset, word, id) => {
        rpcTarget.rpcTypeCompletion(file, offset, word, id)
      }
      case ScopeCompletion(file, offset, word, constructor, id) => {
        rpcTarget.rpcScopeCompletion(file, offset, word, constructor, id)
      }
      case OrganizeImports(file, procId, id, start, end) => {
        rpcTarget.rpcPerformRefactor(Symbol("organizeImports"), procId,
          Map(Symbol("file") -> file,
            Symbol("start") -> start,
            Symbol("end") -> end), id)
      }
      case Rename(file, procId, id, start, end, newName) => {
        rpcTarget.rpcPerformRefactor(Symbol("rename"), procId,
          Map(Symbol("file") -> file,
            Symbol("start") -> start,
            Symbol("end") -> end,
            Symbol("newName") -> newName), id)
      }
      case ExecRefactoring(name, procId, id) => {
        rpcTarget.rpcExecRefactor(Symbol(name), procId, id)
      }
      case TypecheckFile(path, id) => {
        rpcTarget.rpcTypecheckFile(path, id)
      }
      case TypecheckAll(id) => {
        rpcTarget.rpcTypecheckAll(id)
      }
      case ReformatFile(path, id) => {
        val iter = Iterable(path)
        rpcTarget.rpcFormatFiles(iter, id)

      }
      case SymbolAtPoint(file, offset, id) => {
        rpcTarget.rpcSymbolAtPoint(file, offset, id)
      }
      case other => println("###ERROR WRONG MESSAGE :" + other)
    }
  }

  def sendBackgroundMessage(code:Int,detail: Option[String]) {
    sendMessage(BackgroundMessage(code.toString))
  }

  def sendRPCAckOK(callId: Int) {
    sendRPCReturn(BooleanMsg(true), callId)
  }

  def sendRPCReturn(value: WireFormat, callId: Int) {
    sendMessage(Container(value, callId))
  }

  def sendRPCError(code: Int, detail: Option[String],callID:Int) {
    throw new Exception("Not Implemented")
  }

  def sendProtocolError(code:Int, detail:Option[String]) {
    throw new Exception("Not Implemented")
  }

  def sendConnectionInfo(callId: Int) {
    val info = ConnectionInfo(PROTOCOL_VERSION, SERVER_NAME, callId)
    sendRPCReturn(info, callId)
  }

  def sendCompilerReady() {
    sendMessage(CompilerReady())
  }

  def sendTypeCheckResult(notelist: NoteList) {
    sendMessage(TypeCheckResult(notelist.full, notelist.notes))
  }

  /* Below implementation of trait ProtocolConversions */

  def toWF(config: ReplConfig): WireFormat = ReplConfigMsg(config)

  def toWF(config: DebugConfig): WireFormat = DebugConfigMsg(config)

  def toWF(unit: DebugUnit): WireFormat = DebugUnitMsg(unit)

  def toWF(value: Boolean): WireFormat = BooleanMsg(value)

  def toWF(value: DebugSourceLinePairs): WireFormat = DebugSourceLinePairsMsg(value)

  def toWF(value: Note): WireFormat = NoteMsg(value)

  def toWF(values: Iterable[WireFormat]): WireFormat = IterableValues(values)

  def toWF(value: SymbolInfoLight): WireFormat = SymbolInfoLightMsg(value)

  def toWF(value: SymbolInfo): WireFormat = SymbolInfoMsg(value)

  def toWF(value: NamedTypeMemberInfoLight): WireFormat = NamedTypeMemberInfoLightMsg(value)

  def toWF(value: NamedTypeMemberInfo): WireFormat = NamedTypeMemberInfoMsg(value)

  def toWF(value: EntityInfo): WireFormat = EntityInfoMsg(value)

  def toWF(value: TypeInfo): WireFormat = TypeInfoMsg(value)

  def toWF(value: PackageInfo): WireFormat = PackageInfoMsg(value)

  def toWF(value: CallCompletionInfo): WireFormat = CallCompletionInfoMsg(value)

  def toWF(value: InterfaceInfo): WireFormat = InterfaceInfoMsg(value)

  def toWF(value: TypeInspectInfo): WireFormat = TypeInspectInfoMsg(value)

  def toWF(value: RefactorFailure): WireFormat = RefactorFailureMsg(value)

  def toWF(value: RefactorEffect): WireFormat = RefactorEffectMsg(value)

  def toWF(value: RefactorResult): WireFormat = RefactorResultMsg(value)

  //New stuff
  def toWF(value: ImportSuggestions): WireFormat = null

  def toWF(value: Undo): WireFormat = null

  def toWF(value: UndoResult): WireFormat = null

  def toWF(value: Null): WireFormat = null

  def toWF(value: PackageMemberInfoLight): WireFormat = null

  def toWF(notelist: NoteList): WireFormat = TypeCheckResult(notelist.full,notelist.notes)

  def toWF(config: ProjectConfig): WireFormat  = null
}