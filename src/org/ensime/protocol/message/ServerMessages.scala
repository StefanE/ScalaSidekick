package org.ensime.protocol.message

import java.io._
import org.ensime.config.{ProjectConfig, DebugConfig, ReplConfig}
import org.ensime.debug.{DebugUnit, DebugSourceLinePairs}
import org.ensime.model._
import org.ensime.server._
import org.ensime.util._
import scala.actors._
import org.ensime.protocol.message._
import org.ensime.client._

case class BackgroundMessage(msg: String) extends WireFormat {
  def toWireString = toString()
}

case class CompilerReady() extends WireFormat {
  def toWireString = toString()
}

case class TypeCheckResult(lang: Symbol, isFull: Boolean, notes: Iterable[Note])
  extends WireFormat {
  def toWireString = toString()
}


//Server Answers below

case class ConnectionInfo(protocol: String, srvName: String, msgID: Int)
  extends WireFormat with MessageID {
  def toWireString = toString()
}

case class SymbolInfoLightMsg(value: SymbolInfoLight) extends WireFormat {
  def toWireString = value.name+" Sig: "+value.tpeSig 
}

case class NamedTypeMemberInfoLightMsg(value: NamedTypeMemberInfoLight) extends WireFormat {
  def toWireString = value.name+" Sig: "+value.tpeSig
}

case class RefactorResultMsg(value: RefactorResult) extends WireFormat {
  def toWireString = toString()
}

case class RefactorEffectMsg(value: RefactorEffect) extends WireFormat {
  def toWireString = toString()
}

case class RefactorFailureMsg(value: RefactorFailure) extends WireFormat {
  def toWireString = toString()
}

case class TypeInspectInfoMsg(value: TypeInspectInfo) extends WireFormat {
  def toWireString = toString()
}

case class InterfaceInfoMsg(value: InterfaceInfo) extends WireFormat {
  def toWireString = toString()
}

case class CallCompletionInfoMsg(value: CallCompletionInfo) extends WireFormat {
  def toWireString = toString()
}

case class PackageInfoMsg(value: PackageInfo) extends WireFormat {
  def toWireString = toString()
}

case class TypeInfoMsg(value: TypeInfo) extends WireFormat {
  def toWireString = toString()
}

case class EntityInfoMsg(value: EntityInfo) extends WireFormat {
  def toWireString = toString()
}

case class NamedTypeMemberInfoMsg(value: NamedTypeMemberInfo) extends WireFormat {
  def toWireString = toString()
}



case class SymbolInfoMsg(value: SymbolInfo) extends WireFormat {
  def toWireString = toString()
}

case class NoteMsg(value: Note) extends WireFormat {
  def toWireString = toString()
}

case class DebugSourceLinePairsMsg(value: DebugSourceLinePairs) extends WireFormat {
  def toWireString = toString()
}

case class BooleanMsg(value: Boolean) extends WireFormat {
  def toWireString = toString()
}

case class DebugUnitMsg(value: DebugUnit) extends WireFormat {
  def toWireString = toString()
}

case class DebugConfigMsg(value: DebugConfig) extends WireFormat {
  def toWireString = toString()
}

case class ReplConfigMsg(value: ReplConfig) extends WireFormat {
  def toWireString = toString()
}

case class IterableValues(values:Iterable[WireFormat]) extends WireFormat {
  def toWireString = toString()
}

case class Container(value:WireFormat,id:Int) extends WireFormat {
  def toWireString() = ""+id
}